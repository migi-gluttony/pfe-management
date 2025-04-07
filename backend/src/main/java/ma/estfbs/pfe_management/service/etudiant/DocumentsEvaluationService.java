package ma.estfbs.pfe_management.service.etudiant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.etudiant.DocumentDTO;
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
public class DocumentsEvaluationService {

    private final DocumentsEvaluationRepository documentsEvaluationRepository;
    private final BinomeRepository binomeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AcademicYearService academicYearService;

    @Value("${app.upload.dir:${user.home}/uploads/pfe_management}")
    private String uploadDir;

    /**
     * Get all documents submitted by a student
     * 
     * @param etudiantId Student ID
     * @return List of documents
     */
    public List<DocumentDTO> getDocumentsByStudent(Long etudiantId) {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty()) {
            return new ArrayList<>();
        }

        Binome binome = binomes.get(0);

        // Get documents submitted by the binome
        List<DocumentsEvaluation> documents = documentsEvaluationRepository.findByBinomeAndAnneeScolaire(
                binome, currentYear);

        // Map to DTOs
        return documents.stream()
                .map(doc -> DocumentDTO.builder()
                        .id(doc.getId())
                        .titre(doc.getTitre())
                        .localisationDoc(doc.getLocalisationDoc())
                        .commentaire(doc.getCommentaire())
                        .dateSoumission(doc.getDateSoumission())
                        .reviewed(doc.getCommentaire() != null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Upload a new document for evaluation
     * 
     * @param etudiantId Student ID
     * @param titre      Document title
     * @param file       Uploaded file
     * @return Uploaded document information
     * @throws IOException If file cannot be saved
     */
    @Transactional
    public DocumentDTO uploadDocument(Long etudiantId, String titre, MultipartFile file) throws IOException {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();

        // Find the student's binome
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty()) {
            throw new RuntimeException("Vous devez faire partie d'un binôme pour soumettre des documents");
        }

        Binome binome = binomes.get(0);

        // Validate input
        if (titre == null || titre.trim().isEmpty()) {
            throw new RuntimeException("Le titre du document est requis");
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

        // Validate file type
        validateFileType(fileExtension);

        // Generate a unique file name to prevent overwriting
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "documents_evaluation", binome.getId().toString());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save document information in database
        DocumentsEvaluation document = DocumentsEvaluation.builder()
                .binome(binome)
                .titre(titre)
                .localisationDoc(filePath.toString())
                .dateSoumission(LocalDateTime.now())
                .anneeScolaire(currentYear)
                .build();

        document = documentsEvaluationRepository.save(document);

        // Return DTO
        return DocumentDTO.builder()
                .id(document.getId())
                .titre(document.getTitre())
                .localisationDoc(document.getLocalisationDoc())
                .dateSoumission(document.getDateSoumission())
                .reviewed(false)
                .build();
    }

    /**
     * Get document as a downloadable resource
     * 
     * @param etudiantId  Student ID requesting the download
     * @param documentId Document ID to download
     * @return Resource for download
     * @throws Exception If document not found or access denied
     */
    public Resource getDocumentResource(Long etudiantId, Long documentId) throws Exception {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        // Check if student belongs to the binome that submitted the document
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty() || !binomes.get(0).getId().equals(document.getBinome().getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à télécharger ce document");
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
     * 
     * @param documentId Document ID
     * @return File name
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

        return fileName + fileExtension;
    }

    /**
     * Delete a document (only if not reviewed yet)
     * 
     * @param etudiantId  Student ID
     * @param documentId Document ID to delete
     * @throws Exception If document not found, already reviewed, or access denied
     */
    @Transactional
    public void deleteDocument(Long etudiantId, Long documentId) throws Exception {
        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));

        DocumentsEvaluation document = documentsEvaluationRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));

        // Check if student belongs to the binome that submitted the document
        AnneeScolaire currentYear = academicYearService.getCurrentAcademicYear();
        List<Binome> binomes = binomeRepository.findByEtudiant1OrEtudiant2AndAnneeScolaire(
                etudiant, etudiant, currentYear);

        if (binomes.isEmpty() || !binomes.get(0).getId().equals(document.getBinome().getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce document");
        }

        // Check if document has been reviewed
        if (document.getCommentaire() != null) {
            throw new RuntimeException("Ce document a déjà été évalué et ne peut pas être supprimé");
        }

        // Delete the file
        try {
            Path filePath = Paths.get(document.getLocalisationDoc());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }

        // Delete from database
        documentsEvaluationRepository.delete(document);
    }

    /**
     * Validate file type (allowed: PDF, DOC, DOCX, JPG, JPEG, PNG)
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
                          lowerExtension.equals(".docx") ||
                          lowerExtension.equals(".jpg") || 
                          lowerExtension.equals(".jpeg") || 
                          lowerExtension.equals(".png");

        if (!isValid) {
            throw new RuntimeException("Type de fichier non autorisé. Formats acceptés: PDF, DOC, DOCX, JPG, JPEG, PNG");
        }
    }
}
