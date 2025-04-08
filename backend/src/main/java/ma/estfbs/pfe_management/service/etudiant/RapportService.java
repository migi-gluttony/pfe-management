package ma.estfbs.pfe_management.service.etudiant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.JuryDTO;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.RapportDTO;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.RapportResponseDTO;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.SalleDTO;
import ma.estfbs.pfe_management.dto.etudiant.RapportDTOs.SoutenanceInfoDTO;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Rapport;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.RapportRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final RapportRepository rapportRepository;
    private final BinomeRepository binomeRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AcademicYearService academicYearService;

    @Value("${app.upload.dir:${user.home}/uploads/pfe_management}")
    private String uploadDir;

    private static final int REPORT_LOCK_DAYS = 3; // Days before soutenance when report becomes locked

    /**
     * Get rapport information for a student
     * 
     * @param etudiantId Student ID
     * @return Rapport response with rapport and soutenance info
     */
    public RapportResponseDTO getRapportInfo(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty()) {
            return RapportResponseDTO.builder().build(); // Empty response if no binome
        }

        Binome binome = binomes.get(0);

        // Get rapport
        Rapport rapport = rapportRepository.findTopByBinomeAndAnneeScolaireOrderByIdDesc(binome, currentYear);
        RapportDTO rapportDTO = null;

        if (rapport != null) {
            rapportDTO = RapportDTO.builder()
                    .id(rapport.getId())
                    .titre(rapport.getTitre())
                    .localisationRapport(rapport.getLocalisationRapport())
                    .dateSoumission(rapport.getDateSoumission())
                    .note(rapport.getNote())
                    .commentaire(rapport.getCommentaire())
                    .build();
        }

        // Get soutenance
        Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).orElse(null);
        SoutenanceInfoDTO soutenanceDTO = null;

        if (soutenance != null) {
            soutenanceDTO = SoutenanceInfoDTO.builder()
                    .id(soutenance.getId())
                    .date(soutenance.getDate())
                    .heure(soutenance.getHeure())
                    .salle(SalleDTO.builder()
                            .id(soutenance.getSalle().getId())
                            .nom(soutenance.getSalle().getNom())
                            .build())
                    .jury1(JuryDTO.builder()
                            .id(soutenance.getJury1().getId())
                            .nom(soutenance.getJury1().getNom())
                            .prenom(soutenance.getJury1().getPrenom())
                            .email(soutenance.getJury1().getEmail())
                            .build())
                    .jury2(JuryDTO.builder()
                            .id(soutenance.getJury2().getId())
                            .nom(soutenance.getJury2().getNom())
                            .prenom(soutenance.getJury2().getPrenom())
                            .email(soutenance.getJury2().getEmail())
                            .build())
                    .build();
        }

        // Build response
        return RapportResponseDTO.builder()
                .rapport(rapportDTO)
                .soutenance(soutenanceDTO)
                .build();
    }

    /**
     * Upload or replace a rapport
     * 
     * @param etudiantId Student ID
     * @param titre      Rapport title
     * @param file       Uploaded file
     * @return Uploaded rapport information
     * @throws IOException If file cannot be saved
     */
    @Transactional
    public RapportDTO uploadRapport(Long etudiantId, String titre, MultipartFile file) throws IOException {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty()) {
            throw new RuntimeException("Vous devez faire partie d'un binôme pour soumettre un rapport");
        }

        Binome binome = binomes.get(0);

        // Check if soutenance is scheduled
        Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).orElse(null);
        if (soutenance == null) {
            throw new RuntimeException("Vous n'avez pas encore de soutenance planifiée. Veuillez contacter votre chef de département.");
        }

        // Check if upload is allowed (soutenance date > REPORT_LOCK_DAYS)
        if (!canUploadRapport(soutenance)) {
            throw new RuntimeException("Vous ne pouvez plus modifier votre rapport car votre soutenance est prévue dans moins de " + REPORT_LOCK_DAYS + " jours");
        }

        // Validate input
        if (titre == null || titre.trim().isEmpty()) {
            throw new RuntimeException("Le titre du rapport est requis");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // Get file extension
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf("."));
        }

        // Validate file type (only PDF, DOC, DOCX)
        validateFileType(fileExtension);

        // Generate a unique file name to prevent overwriting
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "rapports", binome.getId().toString());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Check if rapport already exists
        Rapport existingRapport = rapportRepository.findTopByBinomeAndAnneeScolaireOrderByIdDesc(binome, currentYear);

        if (existingRapport != null) {
            // Update existing rapport
            // Delete old file if it exists
            try {
                Path oldFilePath = Paths.get(existingRapport.getLocalisationRapport());
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                // Log error but continue
                System.err.println("Erreur lors de la suppression de l'ancien fichier: " + e.getMessage());
            }

            existingRapport.setTitre(titre);
            existingRapport.setLocalisationRapport(filePath.toString());
            existingRapport.setDateSoumission(LocalDateTime.now());
            existingRapport = rapportRepository.save(existingRapport);

            return RapportDTO.builder()
                    .id(existingRapport.getId())
                    .titre(existingRapport.getTitre())
                    .localisationRapport(existingRapport.getLocalisationRapport())
                    .dateSoumission(existingRapport.getDateSoumission())
                    .note(existingRapport.getNote())
                    .commentaire(existingRapport.getCommentaire())
                    .build();
        } else {
            // Create new rapport
            Rapport newRapport = Rapport.builder()
                    .binome(binome)
                    .titre(titre)
                    .localisationRapport(filePath.toString())
                    .note(0) // Default value
                    .anneeScolaire(currentYear)
                    .dateSoumission(LocalDateTime.now())
                    .build();

            newRapport = rapportRepository.save(newRapport);

            return RapportDTO.builder()
                    .id(newRapport.getId())
                    .titre(newRapport.getTitre())
                    .localisationRapport(newRapport.getLocalisationRapport())
                    .dateSoumission(newRapport.getDateSoumission())
                    .note(newRapport.getNote())
                    .commentaire(newRapport.getCommentaire())
                    .build();
        }
    }

    /**
     * Get rapport as a downloadable resource
     * 
     * @param etudiantId Student ID requesting the download
     * @param rapportId  Rapport ID to download
     * @return Resource for download
     * @throws Exception If rapport not found or access denied
     */
    public Resource getRapportResource(Long etudiantId, Long rapportId) throws Exception {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé avec l'ID: " + rapportId));

        // Check if student belongs to the binome that submitted the rapport
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty() || !binomes.get(0).getId().equals(rapport.getBinome().getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à télécharger ce rapport");
        }

        // Get the file path
        Path filePath = Paths.get(rapport.getLocalisationRapport());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Le fichier n'existe pas ou ne peut pas être lu");
        }
    }

    /**
     * Get rapport file name for Content-Disposition header
     * 
     * @param rapportId Rapport ID
     * @return File name
     */
    public String getRapportFileName(Long rapportId) {
        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé avec l'ID: " + rapportId));

        String filePath = rapport.getLocalisationRapport();
        String fileName = rapport.getTitre();

        // Get file extension from path
        String fileExtension = "";
        if (filePath.contains(".")) {
            fileExtension = filePath.substring(filePath.lastIndexOf("."));
        }

        // Clean file name for download (removing special characters)
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        return fileName + fileExtension;
    }

    /**
     * Delete a rapport (only if soutenance date is more than 3 days away)
     * 
     * @param etudiantId Student ID
     * @throws Exception If rapport not found, or soutenance too close, or access denied
     */
    @Transactional
    public void deleteRapport(Long etudiantId) throws Exception {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty()) {
            throw new RuntimeException("Vous n'êtes pas associé à un binôme");
        }

        Binome binome = binomes.get(0);

        // Get rapport
        Rapport rapport = rapportRepository.findTopByBinomeAndAnneeScolaireOrderByIdDesc(binome, currentYear);
        if (rapport == null) {
            throw new RuntimeException("Aucun rapport trouvé à supprimer");
        }

        // Check if soutenance is scheduled
        Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).orElse(null);
        if (soutenance != null && !canUploadRapport(soutenance)) {
            throw new RuntimeException("Vous ne pouvez plus modifier votre rapport car votre soutenance est prévue dans moins de " + REPORT_LOCK_DAYS + " jours");
        }

        // Delete the file
        try {
            Path filePath = Paths.get(rapport.getLocalisationRapport());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }

        // Delete from database
        rapportRepository.delete(rapport);
    }

    /**
     * Check if student can upload/modify rapport (soutenance date > 3 days)
     * 
     * @param etudiantId Student ID
     * @return true if upload is allowed
     */
    public boolean canUploadRapport(Long etudiantId) {
        try {
            Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

            AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

            // Find the student's binome
            List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                    etudiant, etudiant, currentYear);

            if (binomes.isEmpty()) {
                return false; // No binome, cannot upload
            }

            Binome binome = binomes.get(0);

            // Check if soutenance is scheduled
            Soutenance soutenance = soutenanceRepository.findByBinomeAndAnneeScolaire(binome, currentYear).orElse(null);
            if (soutenance == null) {
                return false; // No soutenance, cannot upload
            }

            return canUploadRapport(soutenance);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if rapport can be uploaded/modified based on soutenance date
     * 
     * @param soutenance Soutenance entity
     * @return true if upload is allowed
     */
    private boolean canUploadRapport(Soutenance soutenance) {
        LocalDate today = LocalDate.now();
        LocalDate soutenanceDate = soutenance.getDate();

        // Allow upload if soutenance is more than REPORT_LOCK_DAYS days away
        long daysUntilSoutenance = ChronoUnit.DAYS.between(today, soutenanceDate);
        return daysUntilSoutenance > REPORT_LOCK_DAYS;
    }

    /**
     * Validate file type (allowed: PDF, DOC, DOCX)
     * 
     * @param fileExtension File extension
     * @throws RuntimeException If file type is not allowed
     */
    private void validateFileType(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new RuntimeException("Type de fichier non reconnu");
        }

        String lowerExtension = fileExtension.toLowerCase();
        boolean isValid = lowerExtension.equals(".pdf") || 
                          lowerExtension.equals(".doc") || 
                          lowerExtension.equals(".docx");

        if (!isValid) {
            throw new RuntimeException("Type de fichier non autorisé. Formats acceptés: PDF, DOC, DOCX");
        }
    }
}
