<template>
    <div class="report-submission-view">
        <Toast />
        <ConfirmDialog />

        <div class="view-container">
            <!-- Header section -->
            <div class="header-section">
                <div class="title-filter-group">
                    <h1 class="page-title">Dépôt du Rapport Final</h1>
                    <p class="page-subtitle">
                        Soumettez votre rapport final pour votre projet de fin
                        d'études
                    </p>
                </div>
            </div>

            <!-- Upload section -->
            <Card class="upload-card">
                <template #content>
                    <div v-if="loading" class="loading-container">
                        <ProgressSpinner />
                        <p>Chargement...</p>
                    </div>

                    <div v-else>
                        <div class="soutenance-status" v-if="soutenanceInfo">
                            <div class="status-header">
                                <i class="pi pi-calendar"></i>
                                <h3>Votre soutenance</h3>
                            </div>
                            <div class="status-content">
                                <div class="status-row">
                                    <span class="label">Date:</span>
                                    <span class="value">{{
                                        formatDate(soutenanceInfo.date)
                                    }}</span>
                                </div>
                                <div class="status-row">
                                    <span class="label">Heure:</span>
                                    <span class="value">{{
                                        formatTime(soutenanceInfo.heure)
                                    }}</span>
                                </div>
                                <div class="status-row">
                                    <span class="label">Salle:</span>
                                    <span class="value">{{
                                        soutenanceInfo.salle.nom
                                    }}</span>
                                </div>
                                <div class="status-row">
                                    <span class="label">Statut du dépôt:</span>
                                    <Tag
                                        :severity="
                                            existingReport
                                                ? 'success'
                                                : 'warning'
                                        "
                                        :value="
                                            existingReport
                                                ? 'Rapport déposé'
                                                : 'Rapport non déposé'
                                        "
                                    />
                                </div>
                            </div>
                        </div>

                        <div v-if="!soutenanceInfo" class="no-soutenance">
                            <i class="pi pi-exclamation-circle"></i>
                            <h3>Aucune soutenance planifiée</h3>
                            <p>
                                Vous n'avez pas encore de soutenance planifiée.
                                Votre chef de département doit planifier votre
                                soutenance avant que vous puissiez déposer votre
                                rapport.
                            </p>
                        </div>

                        <div v-else-if="!canUpload" class="upload-disabled">
                            <i class="pi pi-lock"></i>
                            <h3>Dépôt de rapport bloqué</h3>
                            <p>
                                Vous ne pouvez plus modifier votre rapport car
                                votre soutenance est prévue dans moins de 3
                                jours. Contactez votre encadrant si vous avez
                                besoin de faire des modifications.
                            </p>

                            <div
                                v-if="existingReport"
                                class="existing-report-details"
                            >
                                <h4>Votre rapport déposé</h4>
                                <div class="report-info">
                                    <div class="report-row">
                                        <span class="label">Titre:</span>
                                        <span class="value">{{
                                            existingReport.titre
                                        }}</span>
                                    </div>
                                    <div class="report-row">
                                        <span class="label"
                                            >Date de dépôt:</span
                                        >
                                        <span class="value">{{
                                            formatDateTime(
                                                existingReport.dateSoumission
                                            )
                                        }}</span>
                                    </div>
                                    <Button
                                        label="Télécharger"
                                        icon="pi pi-download"
                                        class="p-button-outlined mt-3"
                                        @click="downloadReport(existingReport)"
                                    />
                                </div>
                            </div>
                        </div>

                        <div v-else class="upload-section">
                            <div v-if="existingReport" class="existing-report">
                                <div class="report-header">
                                    <i class="pi pi-file-pdf"></i>
                                    <h3>Rapport actuel</h3>
                                </div>

                                <div class="report-details">
                                    <div class="report-row">
                                        <span class="label">Titre:</span>
                                        <span class="value">{{
                                            existingReport.titre
                                        }}</span>
                                    </div>
                                    <div class="report-row">
                                        <span class="label"
                                            >Date de dépôt:</span
                                        >
                                        <span class="value">{{
                                            formatDateTime(
                                                existingReport.dateSoumission
                                            )
                                        }}</span>
                                    </div>
                                    <div class="report-actions">
                                        <Button
                                            label="Télécharger"
                                            icon="pi pi-download"
                                            class="p-button-outlined"
                                            @click="
                                                downloadReport(existingReport)
                                            "
                                        />
                                        <Button
                                            label="Remplacer"
                                            icon="pi pi-refresh"
                                            class="p-button-outlined p-button-warning"
                                            @click="openUploadDialog(true)"
                                        />
                                        <Button
                                            label="Supprimer"
                                            icon="pi pi-trash"
                                            class="p-button-outlined p-button-danger"
                                            @click="confirmDeleteReport"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div v-else class="no-report">
                                <i class="pi pi-upload"></i>
                                <h3>Aucun rapport déposé</h3>
                                <p>
                                    Vous n'avez pas encore déposé votre rapport
                                    final. Veuillez le déposer au moins 3 jours
                                    avant la date de votre soutenance.
                                </p>
                                <Button
                                    label="Déposer le rapport"
                                    icon="pi pi-upload"
                                    class="p-button-primary mt-3"
                                    @click="openUploadDialog(false)"
                                />
                            </div>
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Guidelines Card -->
            <Card class="guidelines-card">
                <template #title>
                    <div class="card-header">
                        <h3>Consignes pour le dépôt du rapport</h3>
                    </div>
                </template>
                <template #content>
                    <div class="guidelines-content">
                        <div class="guideline-item">
                            <i class="pi pi-file"></i>
                            <div class="guideline-text">
                                <h4>Format du rapport</h4>
                                <p>
                                    Le rapport doit être au format PDF ou Word
                                    (DOC/DOCX). La taille maximum du fichier est
                                    de 20 Mo.
                                </p>
                            </div>
                        </div>

                        <div class="guideline-item">
                            <i class="pi pi-clock"></i>
                            <div class="guideline-text">
                                <h4>Délai de dépôt</h4>
                                <p>
                                    Vous devez déposer votre rapport au moins 3
                                    jours avant la date de votre soutenance.
                                    Après ce délai, vous ne pourrez plus
                                    modifier votre rapport.
                                </p>
                            </div>
                        </div>

                        <div class="guideline-item">
                            <i class="pi pi-check-circle"></i>
                            <div class="guideline-text">
                                <h4>Contenu attendu</h4>
                                <p>
                                    Votre rapport doit inclure : une
                                    introduction, une présentation du problème,
                                    la méthodologie utilisée, les résultats
                                    obtenus, les limitations et perspectives, et
                                    une conclusion.
                                </p>
                            </div>
                        </div>
                    </div>
                </template>
            </Card>
        </div>

        <!-- Report upload dialog -->
        <Dialog
            v-model:visible="showUploadDialog"
            :modal="true"
            :header="
                isReplacing ? 'Remplacer le Rapport' : 'Déposer le Rapport'
            "
            :style="{ width: '50vw' }"
            :closable="false"
        >
            <div class="p-fluid upload-form">
                <div class="field">
                    <label for="reportTitle"
                        >Titre du rapport<span class="required-marker"
                            >*</span
                        ></label
                    >
                    <InputText
                        id="reportTitle"
                        v-model="uploadForm.title"
                        :class="{ 'p-invalid': validationErrors.title }"
                    />
                    <small v-if="validationErrors.title" class="p-error">
                        {{ validationErrors.title }}
                    </small>
                </div>

                <div class="field">
                    <label
                        >Fichier du rapport<span class="required-marker"
                            >*</span
                        ></label
                    >
                    <FileUpload
                        ref="fileUploadRef"
                        :multiple="false"
                        accept=".pdf,.doc,.docx"
                        :maxFileSize="20000000"
                        @select="onFileSelect"
                        @clear="onFileClear"
                        :auto="false"
                        chooseLabel="Choisir"
                        uploadLabel="Confirmer"
                        cancelLabel="Annuler"
                        :showUploadButton="false"
                        :showCancelButton="true"
                        invalidFileSizeMessage="La taille du fichier ne doit pas dépasser 20MB"
                        invalidFileTypeMessage="Seuls les fichiers PDF et Word sont acceptés"
                    >
                        <template #empty>
                            <p>
                                Glissez et déposez votre rapport ici ou cliquez
                                pour parcourir.
                            </p>
                            <p class="file-types">
                                Formats supportés: PDF, DOC, DOCX
                            </p>
                        </template>
                    </FileUpload>
                    <small
                        v-if="!uploadForm.file && fileUploadError"
                        class="p-error"
                    >
                        Veuillez sélectionner un fichier
                    </small>
                </div>

                <div class="confirmation-message">
                    <i class="pi pi-info-circle"></i>
                    <p>
                        En déposant ce rapport, vous confirmez qu'il s'agit de
                        votre version finale pour la soutenance. Vous pourrez le
                        remplacer jusqu'à 3 jours avant la date de soutenance.
                    </p>
                </div>
            </div>

            <template #footer>
                <Button
                    label="Annuler"
                    icon="pi pi-times"
                    class="p-button-text"
                    @click="closeUploadDialog"
                    :disabled="uploading"
                />
                <Button
                    :label="isReplacing ? 'Remplacer' : 'Déposer'"
                    icon="pi pi-check"
                    class="p-button-primary"
                    @click="submitReport"
                    :loading="uploading"
                    :disabled="uploading"
                />
            </template>
        </Dialog>
    </div>
</template>

<script>
import { ref, reactive, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// PrimeVue components
import Toast from "primevue/toast";
import Card from "primevue/card";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import FileUpload from "primevue/fileupload";
import ProgressSpinner from "primevue/progressspinner";
import Tag from "primevue/tag";
import ConfirmDialog from "primevue/confirmdialog";

export default {
    name: "ReportSubmissionView",
    components: {
        Toast,
        Card,
        Button,
        Dialog,
        InputText,
        FileUpload,
        ProgressSpinner,
        Tag,
        ConfirmDialog,
    },
    setup() {
        const toast = useToast();
        const confirm = useConfirm();

        // State
        const loading = ref(true);
        const uploading = ref(false);
        const existingReport = ref(null);
        const soutenanceInfo = ref(null);
        const canUpload = ref(false);
        const showUploadDialog = ref(false);
        const isReplacing = ref(false);
        const fileUploadRef = ref(null);
        const fileUploadError = ref(false);
        const validationErrors = ref({});

        const uploadForm = reactive({
            title: "",
            file: null,
        });

        // Load data on mount
        onMounted(async () => {
            await Promise.all([loadReport(), checkUploadPermission()]);
        });

        // Methods

        // Load existing report if any
        const loadReport = async () => {
            try {
                const response = await ApiService.get("/etudiant/rapport");
                existingReport.value = response.rapport || null;
                soutenanceInfo.value = response.soutenance || null;
            } catch (error) {
                handleError(error, "Erreur lors du chargement du rapport");
            } finally {
                loading.value = false;
            }
        };

        // Check if user can upload/replace report
        const checkUploadPermission = async () => {
            try {
                const response = await ApiService.get(
                    "/etudiant/rapport/can-upload"
                );
                canUpload.value = response.canUpload;
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors de la vérification des permissions"
                );
                canUpload.value = false;
            }
        };

        // Open report upload dialog
        const openUploadDialog = (replacing) => {
            isReplacing.value = replacing;

            // Pre-fill title if replacing
            if (replacing && existingReport.value) {
                uploadForm.title = existingReport.value.titre || "";
            } else {
                resetUploadForm();
            }

            showUploadDialog.value = true;
        };

        // Close report upload dialog
        const closeUploadDialog = () => {
            if (uploading.value) return;
            showUploadDialog.value = false;
            resetUploadForm();
        };

        // Reset upload form
        const resetUploadForm = () => {
            uploadForm.title = "";
            uploadForm.file = null;
            fileUploadError.value = false;
            validationErrors.value = {};
            if (fileUploadRef.value) {
                fileUploadRef.value.clear();
            }
        };

        // Handle file selection
        const onFileSelect = (event) => {
            uploadForm.file = event.files[0];
            fileUploadError.value = false;
        };

        // Handle file clear
        const onFileClear = () => {
            uploadForm.file = null;
        };

        // Validate form
        const validateForm = () => {
            validationErrors.value = {};

            if (!uploadForm.title) {
                validationErrors.value.title = "Le titre est requis";
            } else if (uploadForm.title.length < 5) {
                validationErrors.value.title =
                    "Le titre doit contenir au moins 5 caractères";
            }

            return Object.keys(validationErrors.value).length === 0;
        };

        // Submit report
        const submitReport = async () => {
            fileUploadError.value = !uploadForm.file;

            if (!validateForm() || fileUploadError.value) return;

            uploading.value = true;
            try {
                const formData = new FormData();
                formData.append("file", uploadForm.file);
                formData.append("titre", uploadForm.title);

                // Use uploadFiles method instead of post - this ensures proper multipart handling
                await ApiService.uploadFiles(
                    "/etudiant/rapport/upload",
                    formData
                );

                toast.add({
                    severity: "success",
                    summary: isReplacing.value
                        ? "Rapport remplacé"
                        : "Rapport déposé",
                    detail: isReplacing.value
                        ? "Votre rapport a été remplacé avec succès"
                        : "Votre rapport a été déposé avec succès",
                    life: 3000,
                });

                closeUploadDialog();
                await loadReport();
            } catch (error) {
                handleError(
                    error,
                    isReplacing.value
                        ? "Erreur lors du remplacement du rapport"
                        : "Erreur lors du dépôt du rapport"
                );
            } finally {
                uploading.value = false;
            }
        };

        // Confirm report deletion
        const confirmDeleteReport = () => {
            confirm.require({
                message:
                    "Êtes-vous sûr de vouloir supprimer votre rapport ? Vous devrez en déposer un nouveau avant votre soutenance.",
                header: "Confirmation de suppression",
                icon: "pi pi-exclamation-triangle",
                acceptClass: "p-button-danger",
                accept: () => deleteReport(),
                reject: () => {},
            });
        };

        // Delete report
        const deleteReport = async () => {
            try {
                await ApiService.delete("/etudiant/rapport");

                toast.add({
                    severity: "success",
                    summary: "Rapport supprimé",
                    detail: "Votre rapport a été supprimé avec succès",
                    life: 3000,
                });

                existingReport.value = null;
            } catch (error) {
                handleError(error, "Erreur lors de la suppression du rapport");
            }
        };

        // Download report
        // Download report
        const downloadReport = (report) => {
            toast.add({
                severity: "info",
                summary: "Téléchargement",
                detail: "Préparation du téléchargement...",
                life: 2000,
            });

            const token =
                localStorage.getItem("token") ||
                sessionStorage.getItem("token");

            fetch(
                `${
                    import.meta.env.VITE_API_URL || "/api"
                }/etudiant/rapport/download/${report.id}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            )
                .then((response) => {
                    if (!response.ok) throw new Error("Download failed");

                    // Get content type to determine extension
                    const contentType = response.headers.get("content-type");

                    // Try to get filename from Content-Disposition header
                    let filename = "";
                    const disposition = response.headers.get(
                        "content-disposition"
                    );
                    if (disposition && disposition.includes("filename=")) {
                        const filenameRegex =
                            /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                        const matches = filenameRegex.exec(disposition);
                        if (matches && matches[1]) {
                            filename = matches[1].replace(/['"]/g, "");
                        }
                    }

                    // If no filename from header, use report title or default with proper extension
                    if (!filename) {
                        filename = report.titre || "rapport";

                        // Ensure proper extension based on content type
                        if (contentType) {
                            if (
                                contentType.includes("pdf") &&
                                !filename.toLowerCase().endsWith(".pdf")
                            ) {
                                filename += ".pdf";
                            } else if (
                                contentType.includes("msword") &&
                                !filename.toLowerCase().endsWith(".doc")
                            ) {
                                filename += ".doc";
                            } else if (
                                contentType.includes("wordprocessingml") &&
                                !filename.toLowerCase().endsWith(".docx")
                            ) {
                                filename += ".docx";
                            }
                        }
                    }

                    // Sanitize filename - remove problematic characters
                    filename = filename.replace(/[/\\?%*:|"<>]/g, "-");

                    return response.blob().then((blob) => ({ blob, filename }));
                })
                .then(({ blob, filename }) => {
                    // Create download link
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement("a");
                    a.href = url;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();

                    // Clean up
                    setTimeout(() => {
                        document.body.removeChild(a);
                        URL.revokeObjectURL(url);
                    }, 100);

                    toast.add({
                        severity: "success",
                        summary: "Téléchargement réussi",
                        detail: `Fichier "${filename}" téléchargé avec succès`,
                        life: 3000,
                    });
                })
                .catch((error) => {
                    console.error("Download error:", error);
                    toast.add({
                        severity: "error",
                        summary: "Erreur de téléchargement",
                        detail: "Impossible de télécharger le fichier",
                        life: 3000,
                    });
                });
        };

        // Format date (day only)
        const formatDate = (dateString) => {
            if (!dateString) return "";
            const date = new Date(dateString);
            return new Intl.DateTimeFormat("fr-FR", {
                year: "numeric",
                month: "long",
                day: "numeric",
            }).format(date);
        };

        // Format time
        const formatTime = (timeString) => {
            if (!timeString) return "";
            const [hours, minutes] = timeString.split(":");
            return `${hours}h${minutes}`;
        };

        // Format full date with time
        const formatDateTime = (dateString) => {
            if (!dateString) return "";
            const date = new Date(dateString);
            return new Intl.DateTimeFormat("fr-FR", {
                year: "numeric",
                month: "long",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
            }).format(date);
        };

        // Error handling
        const handleError = (error, defaultMessage) => {
            console.error(defaultMessage, error);
            let message = defaultMessage;

            if (error.message) {
                message = error.message;
            } else if (
                error.response &&
                error.response.data &&
                error.response.data.message
            ) {
                message = error.response.data.message;
            }

            toast.add({
                severity: "error",
                summary: "Erreur",
                detail: message,
                life: 5000,
            });
        };

        return {
            // State
            loading,
            uploading,
            existingReport,
            soutenanceInfo,
            canUpload,
            showUploadDialog,
            isReplacing,
            uploadForm,
            fileUploadRef,
            fileUploadError,
            validationErrors,

            // Methods
            openUploadDialog,
            closeUploadDialog,
            onFileSelect,
            onFileClear,
            submitReport,
            confirmDeleteReport,
            deleteReport,
            downloadReport,
            formatDate,
            formatTime,
            formatDateTime,
        };
    },
};
</script>

<style scoped>
.report-submission-view {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.view-container {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
}

.dark-mode .header-section {
    border-bottom-color: #333;
}

.title-filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.page-title {
    margin: 0;
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
}

.page-subtitle {
    color: var(--text-color-secondary);
    margin: 0;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--primary-color);
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 2rem;
}

.loading-container p {
    margin-top: 1rem;
    color: var(--text-color-secondary);
}

/* Soutenance Status */
.soutenance-status {
    background-color: var(--surface-hover);
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.status-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1rem;
}

.status-header i {
    font-size: 1.5rem;
    color: var(--primary-color);
}

.status-header h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--text-color);
}

.status-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
}

.status-row {
    display: flex;
    flex-direction: column;
}

.status-row .label {
    font-weight: 600;
    color: var(--text-color-secondary);
    margin-bottom: 0.25rem;
}

.status-row .value {
    font-size: 1.1rem;
}

/* No Soutenance state */
.no-soutenance,
.upload-disabled,
.no-report {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 3rem 1rem;
}

.no-soutenance i,
.upload-disabled i,
.no-report i {
    font-size: 3rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.no-soutenance h3,
.upload-disabled h3,
.no-report h3 {
    margin: 0 0 0.5rem;
    color: var(--text-color);
}

.no-soutenance p,
.upload-disabled p,
.no-report p {
    margin: 0;
    color: var(--text-color-secondary);
    max-width: 600px;
}

/* Existing report section */
.existing-report {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    border: 1px solid var(--surface-border);
}

.report-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
}

.report-header i {
    font-size: 1.5rem;
    color: var(--red-500);
}

.report-header h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--text-color);
}

.report-details {
    padding: 0 0.5rem;
}

.report-row {
    display: flex;
    align-items: center;
    margin-bottom: 1rem;
}

.report-row .label {
    min-width: 120px;
    font-weight: 600;
    color: var(--text-color-secondary);
}

.report-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1.5rem;
    flex-wrap: wrap;
}

/* Upload section */
.required-marker {
    color: var(--red-500);
    margin-left: 0.25rem;
}

.upload-form {
    margin-bottom: 1rem;
}

.field {
    margin-bottom: 1.5rem;
}

.field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.file-types {
    color: var(--text-color-secondary);
    font-size: 0.9rem;
    margin-top: 0.5rem;
}

.confirmation-message {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    background-color: var(--surface-hover);
    padding: 1rem;
    border-radius: 8px;
}

.confirmation-message i {
    color: var(--primary-color);
    font-size: 1.25rem;
}

.confirmation-message p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
    line-height: 1.4;
}

/* Guidelines section */
.guidelines-content {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.guideline-item {
    display: flex;
    gap: 1rem;
}

.guideline-item i {
    font-size: 1.5rem;
    color: var(--primary-color);
}

.guideline-text h4 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
    color: var(--text-color);
}

.guideline-text p {
    margin: 0;
    color: var(--text-color-secondary);
}

/* Existing report details (for upload disabled view) */
.existing-report-details {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    text-align: left;
    max-width: 500px;
    width: 100%;
}

.existing-report-details h4 {
    margin: 0 0 1rem;
    font-size: 1.1rem;
    color: var(--primary-color);
    text-align: center;
}

.report-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.mt-3 {
    margin-top: 1rem;
}

/* Responsive styles */
@media (max-width: 768px) {
    .header-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .status-content {
        grid-template-columns: 1fr;
    }

    .report-row {
        flex-direction: column;
        align-items: flex-start;
    }

    .report-row .label {
        margin-bottom: 0.25rem;
    }

    .report-actions {
        flex-direction: column;
    }

    .report-actions .p-button {
        width: 100%;
    }

    .guideline-item {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }
}
</style>
