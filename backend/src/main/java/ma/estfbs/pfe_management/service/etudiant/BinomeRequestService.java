package ma.estfbs.pfe_management.service.etudiant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.BinomeDTO;
import ma.estfbs.pfe_management.dto.etudiant.BinomeRequestDTO;
import ma.estfbs.pfe_management.dto.etudiant.BinomeRequestResponse;
import ma.estfbs.pfe_management.dto.etudiant.BinomeSelectionResponse;
import ma.estfbs.pfe_management.dto.etudiant.StudentDTO;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DemandeBinome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.DemandeBinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BinomeRequestService {

        private final UtilisateurRepository utilisateurRepository;
        private final EtudiantRepository etudiantRepository;
        private final BinomeRepository binomeRepository;
        private final DemandeBinomeRepository demandeBinomeRepository;
        private final AcademicYearService academicYearService;

        /**
         * Get pending binome requests for a student
         */
        public List<BinomeRequestDTO> getPendingRequestsForStudent(Long etudiantId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if student already has a binome
                boolean hasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (hasBinome) {
                        return new ArrayList<>(); // Empty list if student already has a binome
                }

                // Find pending requests where this student is the target
                List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeAndStatut(
                                etudiant, DemandeBinome.Statut.EN_ATTENTE);

                // Map to DTOs
                return pendingRequests.stream()
                                .map(this::mapToBinomeRequestDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get available students from the same filiere for binome formation
         */
        public BinomeSelectionResponse getAvailableStudentsFromSameFiliere(Long etudiantId) {
                Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Etudiant not found with id: " + etudiantId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if student already has a binome
                boolean hasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                etudiant, etudiant, currentYear);

                if (hasBinome) {
                        return BinomeSelectionResponse.builder()
                                        .availableStudents(new ArrayList<>())
                                        .hasSentRequest(false)
                                        .targetStudentId(null)
                                        .build();
                }

                // Get the student's filiere
                Etudiant etudiantInfo = etudiantRepository.findByUtilisateur(etudiant)
                                .orElseThrow(() -> new RuntimeException(
                                                "Etudiant info not found for user: " + etudiantId));

                // Find all students from the same filiere in current year
                List<Etudiant> sameFiliere = etudiantRepository.findByFiliereAndAnneeScolaire(
                                etudiantInfo.getFiliere(), currentYear);

                // Filter out students who already have a binome
                List<Utilisateur> availableUtilisateurs = sameFiliere.stream()
                                .map(Etudiant::getUtilisateur)
                                .filter(u -> !u.getId().equals(etudiantId)) // Exclude self
                                .filter(u -> !binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(u, u,
                                                currentYear))
                                .collect(Collectors.toList());

                // Check if current student has sent a request
                List<DemandeBinome> sentRequests = demandeBinomeRepository.findByDemandeurAndStatut(
                                etudiant, DemandeBinome.Statut.EN_ATTENTE);

                boolean hasSentRequest = !sentRequests.isEmpty();
                Long targetStudentId = hasSentRequest ? sentRequests.get(0).getDemande().getId() : null;

                // Filter out students to whom this student has already sent a request
                if (hasSentRequest) {
                        final Long finalTargetId = targetStudentId;
                        availableUtilisateurs = availableUtilisateurs.stream()
                                        .filter(u -> !u.getId().equals(finalTargetId))
                                        .collect(Collectors.toList());
                }

                // Map to DTOs
                List<StudentDTO> studentDTOs = availableUtilisateurs.stream()
                                .map(this::mapToStudentDTO)
                                .collect(Collectors.toList());

                return BinomeSelectionResponse.builder()
                                .availableStudents(studentDTOs)
                                .hasSentRequest(hasSentRequest)
                                .targetStudentId(targetStudentId)
                                .build();
        }

        /**
         * Send a binome request to another student
         */
        @Transactional
        public BinomeRequestResponse sendBinomeRequest(Long fromStudentId, Long toStudentId) {
                Utilisateur fromStudent = utilisateurRepository.findById(fromStudentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Sender student not found with id: " + fromStudentId));

                Utilisateur toStudent = utilisateurRepository.findById(toStudentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Target student not found with id: " + toStudentId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if sender already has a binome
                boolean senderHasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                fromStudent, fromStudent, currentYear);

                if (senderHasBinome) {
                        throw new RuntimeException("You already have a binome for this academic year");
                }

                // Check if target already has a binome
                boolean targetHasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                toStudent, toStudent, currentYear);

                if (targetHasBinome) {
                        throw new RuntimeException("Target student already has a binome for this academic year");
                }

                // Check if sender already has a pending request
                boolean hasPendingRequest = demandeBinomeRepository.findByDemandeurAndStatut(
                                fromStudent, DemandeBinome.Statut.EN_ATTENTE).size() > 0;

                if (hasPendingRequest) {
                        throw new RuntimeException("You already have a pending binome request");
                }

                // Check if there's already a request from target to sender
                List<DemandeBinome> existingRequests = demandeBinomeRepository.findByDemandeurAndDemandeAndStatut(
                                toStudent, fromStudent, DemandeBinome.Statut.EN_ATTENTE);

                if (!existingRequests.isEmpty()) {
                        // Auto-accept the existing request
                        DemandeBinome existingRequest = existingRequests.get(0);
                        return BinomeRequestResponse.builder()
                                        .success(true)
                                        .message("Found an existing request from this student. Auto-accepting it.")
                                        .binome(acceptBinomeRequest(fromStudentId, existingRequest.getId()))
                                        .build();
                }

                // Create new request
                DemandeBinome newRequest = DemandeBinome.builder()
                                .demandeur(fromStudent)
                                .demande(toStudent)
                                .statut(DemandeBinome.Statut.EN_ATTENTE)
                                .dateDemande(LocalDateTime.now())
                                .anneeScolaire(currentYear)
                                .build();

                demandeBinomeRepository.save(newRequest);

                return BinomeRequestResponse.builder()
                                .success(true)
                                .message("Binome request sent successfully")
                                .build();
        }

        /**
         * Accept a binome request
         */
        @Transactional
        public BinomeDTO acceptBinomeRequest(Long studentId, Long requestId) {
                Utilisateur student = utilisateurRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

                DemandeBinome request = demandeBinomeRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

                // Verify this student is the target of the request
                if (!request.getDemande().getId().equals(studentId)) {
                        throw new RuntimeException("This request was not sent to you");
                }

                // Verify the request is pending
                if (request.getStatut() != DemandeBinome.Statut.EN_ATTENTE) {
                        throw new RuntimeException("This request has already been processed");
                }

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if either student already has a binome
                Utilisateur requester = request.getDemandeur();
                boolean requesterHasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                requester, requester, currentYear);

                boolean targetHasBinome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                student, student, currentYear);

                if (requesterHasBinome || targetHasBinome) {
                        request.setStatut(DemandeBinome.Statut.REFUSER);
                        demandeBinomeRepository.save(request);
                        throw new RuntimeException("One of the students already has a binome");
                }

                // Update request status
                request.setStatut(DemandeBinome.Statut.ACCEPTER);
                demandeBinomeRepository.save(request);

                // Reject all other pending requests for both students
                List<DemandeBinome> otherRequestsToStudent = demandeBinomeRepository.findByDemandeAndStatut(
                                student, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> otherRequestsFromStudent = demandeBinomeRepository.findByDemandeurAndStatut(
                                student, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> otherRequestsToRequester = demandeBinomeRepository.findByDemandeAndStatut(
                                requester, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> otherRequestsFromRequester = demandeBinomeRepository.findByDemandeurAndStatut(
                                requester, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> allOtherRequests = new ArrayList<>();
                allOtherRequests.addAll(otherRequestsToStudent);
                allOtherRequests.addAll(otherRequestsFromStudent);
                allOtherRequests.addAll(otherRequestsToRequester);
                allOtherRequests.addAll(otherRequestsFromRequester);

                // Remove the current request from the list
                allOtherRequests = allOtherRequests.stream()
                                .filter(r -> !r.getId().equals(requestId))
                                .collect(Collectors.toList());

                // Reject all other requests
                for (DemandeBinome otherRequest : allOtherRequests) {
                        otherRequest.setStatut(DemandeBinome.Statut.REFUSER);
                        demandeBinomeRepository.save(otherRequest);
                }

                // Create new binome
                Binome newBinome = Binome.builder()
                                .etudiant1(requester)
                                .etudiant2(student)
                                .anneeScolaire(currentYear)
                                .build();

                newBinome = binomeRepository.save(newBinome);

                return mapToBinomeDTO(newBinome);
        }

        /**
         * Reject a binome request
         */
        @Transactional
        public void rejectBinomeRequest(Long studentId, Long requestId) {
                Utilisateur student = utilisateurRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

                DemandeBinome request = demandeBinomeRepository.findById(requestId)
                                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

                // Verify this student is the target of the request
                if (!request.getDemande().getId().equals(studentId)) {
                        throw new RuntimeException("This request was not sent to you");
                }

                // Verify the request is pending
                if (request.getStatut() != DemandeBinome.Statut.EN_ATTENTE) {
                        throw new RuntimeException("This request has already been processed");
                }

                // Update request status
                request.setStatut(DemandeBinome.Statut.REFUSER);
                demandeBinomeRepository.save(request);
        }

        /**
         * Create a binome with just one student (continue alone option)
         */
        @Transactional
        public BinomeDTO createSingleStudentBinome(Long studentId) {
                Utilisateur student = utilisateurRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

                // Get current academic year
                AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

                // Check if student already has a binome
                boolean hasBindome = binomeRepository.existsByEtudiant1OrEtudiant2AndAnneeScolaire(
                                student, student, currentYear);

                if (hasBindome) {
                        throw new RuntimeException("You already have a binome for this academic year");
                }

                // Reject all pending requests to/from this student
                List<DemandeBinome> pendingRequestsTo = demandeBinomeRepository.findByDemandeAndStatut(
                                student, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> pendingRequestsFrom = demandeBinomeRepository.findByDemandeurAndStatut(
                                student, DemandeBinome.Statut.EN_ATTENTE);

                List<DemandeBinome> allPendingRequests = new ArrayList<>();
                allPendingRequests.addAll(pendingRequestsTo);
                allPendingRequests.addAll(pendingRequestsFrom);

                for (DemandeBinome request : allPendingRequests) {
                        request.setStatut(DemandeBinome.Statut.REFUSER);
                        demandeBinomeRepository.save(request);
                }

                // Create new binome with only one student
                Binome newBinome = Binome.builder()
                                .etudiant1(student)
                                .etudiant2(null) // No second student
                                .anneeScolaire(currentYear)
                                .build();

                newBinome = binomeRepository.save(newBinome);

                return mapToBinomeDTO(newBinome);
        }

        // Mapping methods

        private BinomeRequestDTO mapToBinomeRequestDTO(DemandeBinome request) {
                return BinomeRequestDTO.builder()
                                .id(request.getId())
                                .demandeurId(request.getDemandeur().getId())
                                .demandeurNom(request.getDemandeur().getNom())
                                .demandeurPrenom(request.getDemandeur().getPrenom())
                                .dateDemande(request.getDateDemande())
                                .build();
        }

        private StudentDTO mapToStudentDTO(Utilisateur utilisateur) {
                return StudentDTO.builder()
                                .id(utilisateur.getId())
                                .nom(utilisateur.getNom())
                                .prenom(utilisateur.getPrenom())
                                .email(utilisateur.getEmail())
                                .build();
        }

        private BinomeDTO mapToBinomeDTO(Binome binome) {
                return BinomeDTO.builder()
                                .id(binome.getId())
                                .etudiant1(mapToStudentDTO(binome.getEtudiant1()))
                                .etudiant2(binome.getEtudiant2() != null ? mapToStudentDTO(binome.getEtudiant2())
                                                : null)
                                .sujetId(binome.getSujet() != null ? binome.getSujet().getId() : null)
                                .build();
        }

        /**
         * Cancel a binome request sent by a student
         * Finds and cancels the pending request without requiring requestId
         */
        @Transactional
        public void cancelBinomeRequest(Long studentId) {
                Utilisateur student = utilisateurRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

                // Find pending requests from this student
                List<DemandeBinome> pendingRequests = demandeBinomeRepository.findByDemandeurAndStatut(
                                student, DemandeBinome.Statut.EN_ATTENTE);

                if (pendingRequests.isEmpty()) {
                        throw new RuntimeException("No pending requests found for you");
                }

                // Take the first pending request
                // This assumes a student only has one active request at a time
                DemandeBinome request = pendingRequests.get(0);

                // Update request status
                request.setStatut(DemandeBinome.Statut.REFUSER);
                demandeBinomeRepository.save(request);
        }
}