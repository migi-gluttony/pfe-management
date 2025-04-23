package ma.estfbs.pfe_management.service.encadrant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.encadrant.DocumentEvaluationDTO;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.DocumentsEvaluation;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.DocumentsEvaluationRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;
import ma.estfbs.pfe_management.service.AcademicYearService;

@Service
@RequiredArgsConstructor
public class EncadrantDocumentEvaluationService {

    private final DocumentsEvaluationRepository documentsEvaluationRepository;
    private final BinomeRepository binomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AcademicYearService academicYearService;

    /**
     * Get all documents from student binomes assigned to the encadrant
     * 
     * @param encadrantId Encadrant ID
     * @return List of documents
     */
    public List<DocumentEvaluationDTO> getDocumentsByEncadrant(Long encadrantId) {
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'ID: " + encadrantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Get all binomes assigned to this encadrant
        List<Binome> binomes = binomeRepository.findByEncadrantAndAnneeScolaire(encadrant, currentYear);

        if (binomes.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all documents for these binomes
        List<DocumentsEvaluation> allDocuments = new ArrayList<>();

        for (Binome binome : binomes) {
            List<DocumentsEvaluation> binomeDocuments = documentsEvaluationRepository
                    .findByBinomeAndAnneeScolaire(binome, currentYear);
            allDocuments.addAll(binomeDocuments);
        }

        // Map to DTOs
        return allDocuments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for a specific binome
     * 
     * @param encadrantId Encadrant ID
     * @param binomeId    Binome ID
     * @return List of documents
     */
    public List<DocumentEvaluationDTO> getDocumentsByBinome(Long encadrantId, Long binomeId) {
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'ID: " + encadrantId));

        Binome binome = binomeRepository.findById(binomeId)
                .orElseThrow(() -> new RuntimeException("Binôme non trouvé avec l'ID: " + binomeId));

        // Check if encadrant is assigned to this binome
        if (!binome.getEncadrant().getId().equals(encadrantId)) {
            throw new RuntimeException("Vous n'êtes pas l'encadrant de ce binôme");
        }

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Get all documents for this binome
        List<DocumentsEvaluation> documents = documentsEvaluationRepository
                .findByBinomeAndAnneeScolaire(binome, currentYear);

        // Map to DTOs
        return documents.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get document as a downloadable resource
     * 
     * @param encadrantId Encadrant ID
     * @param documentId  Document ID
     * @return Resource for download
     * @throws Exception If document not found or access denied
     */
    public Resource getDocumentResource(Long encadrantId, Long documentId) throws Exception {
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'ID: " + encadrantId));

        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        // Check if encadrant is assigned to this binome
        if (!document.getBinome().getEncadrant().getId().equals(encadrantId)) {
            throw new RuntimeException("Vous n'êtes pas l'encadrant de ce binôme");
        }

        // Get the file path
        Path filePath = Paths.get(document.getLocalisationDoc());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Le fichier n'existe pas ou ne peut pas être lu");
        }
    }

    /**
     * Get document file name for Content-Disposition header
     * This version ensures proper filename generation for downloads
     * 
     * @param documentId Document ID
     * @return File name with extension
     */
    public String getDocumentFileName(Long documentId) {
        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        String filePath = document.getLocalisationDoc();
        String fileName = document.getTitre();

        // Get file extension from path
        String fileExtension = "";
        if (filePath.contains(".")) {
            fileExtension = filePath.substring(filePath.lastIndexOf("."));
        }

        // Clean file name for download (removing special characters)
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        // Make sure the filename has the correct extension
        if (!fileName.toLowerCase().endsWith(fileExtension.toLowerCase())) {
            fileName += fileExtension;
        }

        return fileName;
    }

    /**
     * Get document content type based on file extension
     * 
     * @param documentId Document ID
     * @return Content type for the document
     */
    public String getDocumentContentType(Long documentId) {
        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        String filePath = document.getLocalisationDoc();

        try {
            // First try to detect content type from the actual file
            Path path = Paths.get(filePath);
            return Files.probeContentType(path);
        } catch (IOException e) {
            // Fallback to extension-based content type
            if (filePath.toLowerCase().endsWith(".pdf")) {
                return "application/pdf";
            } else if (filePath.toLowerCase().endsWith(".doc")) {
                return "application/msword";
            } else if (filePath.toLowerCase().endsWith(".docx")) {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (filePath.toLowerCase().endsWith(".png")) {
                return "image/png";
            } else {
                // Default fallback
                return "application/octet-stream";
            }
        }
    }

    /**
     * Evaluate a document (add comment)
     * 
     * @param encadrantId Encadrant ID
     * @param documentId  Document ID
     * @param commentaire Comment to add
     * @return Updated document
     */
    @Transactional
    public DocumentEvaluationDTO evaluateDocument(Long encadrantId, Long documentId, String commentaire) {
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'ID: " + encadrantId));

        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        // Check if encadrant is assigned to this binome
        if (!document.getBinome().getEncadrant().getId().equals(encadrantId)) {
            throw new RuntimeException("Vous n'êtes pas l'encadrant de ce binôme");
        }

        // Add comment
        document.setCommentaire(commentaire);
        document = documentsEvaluationRepository.save(document);

        return mapToDTO(document);
    }

    /**
     * Map DocumentsEvaluation entity to DTO
     * 
     * @param document DocumentsEvaluation entity
     * @return DocumentEvaluationDTO
     */
    private DocumentEvaluationDTO mapToDTO(DocumentsEvaluation document) {
        Binome binome = document.getBinome();

        return DocumentEvaluationDTO.builder()
                .id(document.getId())
                .titre(document.getTitre())
                .localisationDoc(document.getLocalisationDoc())
                .commentaire(document.getCommentaire())
                .dateSoumission(document.getDateSoumission())
                .binomeId(binome.getId())
                .etudiant1Nom(binome.getEtudiant1().getNom())
                .etudiant1Prenom(binome.getEtudiant1().getPrenom())
                .etudiant2Nom(binome.getEtudiant2() != null ? binome.getEtudiant2().getNom() : null)
                .etudiant2Prenom(binome.getEtudiant2() != null ? binome.getEtudiant2().getPrenom() : null)
                .build();
    }

}