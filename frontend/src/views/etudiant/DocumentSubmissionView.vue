<template>
    <div class="document-submission-view">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un document..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="view-container">
            <!-- Header section -->
            <div class="header-section">
                <div class="title-filter-group">
                    <h1 class="page-title">Soumission de Documents</h1>
                    <p class="page-subtitle">
                        Soumettez des documents pour évaluation par votre
                        encadrant
                    </p>
                </div>

                <div class="action-buttons">
                    <Button
                        label="Soumettre un Document"
                        icon="pi pi-upload"
                        class="p-button-primary"
                        @click="openUploadDialog"
                        :disabled="loading"
                    />
                </div>
            </div>

            <!-- Document upload dialog -->
            <Dialog
                v-model:visible="showUploadDialog"
                :modal="true"
                header="Soumettre un Document"
                :style="{ width: '50vw' }"
                :closable="false"
            >
                <div class="p-fluid submission-form">
                    <div class="field">
                        <label for="title"
                            >Titre du document<span class="required-marker"
                                >*</span
                            ></label
                        >
                        <InputText
                            id="title"
                            v-model="uploadForm.title"
                            :class="{ 'p-invalid': validationErrors.title }"
                        />
                        <small v-if="validationErrors.title" class="p-error">
                            {{ validationErrors.title }}
                        </small>
                    </div>

                    <div class="field">
                        <label
                            >Fichier<span class="required-marker"
                                >*</span
                            ></label
                        >
                        <FileUpload
                            ref="fileUploadRef"
                            :multiple="false"
                            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                            :maxFileSize="10000000"
                            @select="onFileSelect"
                            @clear="onFileClear"
                            :auto="false"
                            chooseLabel="Choisir"
                            uploadLabel="Confirmer"
                            cancelLabel="Annuler"
                            :showUploadButton="false"
                            :showCancelButton="true"
                            invalidFileSizeMessage="La taille du fichier ne doit pas dépasser 10MB"
                            invalidFileTypeMessage="Type de fichier non supporté"
                        >
                            <template #empty>
                                <p>
                                    Glissez et déposez un fichier ici ou cliquez
                                    pour parcourir.
                                </p>
                                <p class="file-types">
                                    Formats supportés: PDF, DOC, DOCX, JPG, PNG
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
                        label="Soumettre"
                        icon="pi pi-check"
                        class="p-button-primary"
                        @click="submitDocument"
                        :loading="uploading"
                        :disabled="uploading"
                    />
                </template>
            </Dialog>

            <!-- Previously submitted documents -->
            <div class="documents-list-section">
                <Card>
                    <template #title>
                        <div class="card-header">
                            <h3>Documents Soumis</h3>
                            <div class="header-actions">
                                <Button
                                    icon="pi pi-refresh"
                                    text
                                    @click="loadDocuments"
                                    :disabled="loading"
                                    tooltip="Rafraîchir"
                                />
                            </div>
                        </div>
                    </template>

                    <template #content>
                        <div v-if="loading" class="loading-container">
                            <ProgressSpinner />
                            <p>Chargement des documents...</p>
                        </div>

                        <div
                            v-else-if="filteredDocuments.length === 0"
                            class="empty-documents"
                        >
                            <i class="pi pi-file-o empty-icon"></i>
                            <h3>Aucun document soumis</h3>
                            <p>
                                Vous n'avez pas encore soumis de documents pour
                                évaluation
                            </p>
                        </div>

                        <DataTable
                            v-else
                            :value="filteredDocuments"
                            stripedRows
                            responsiveLayout="scroll"
                            class="documents-table"
                            sortField="dateSoumission" :sortOrder="-1" 

                        >
                            <Column field="titre" header="Titre" sortable />
                            <Column
                                header="Date de soumission"
                                sortable
                                sortField="dateSoumission"
                            >
                                <template #body="slotProps">
                                    {{
                                        formatDate(
                                            slotProps.data.dateSoumission
                                        )
                                    }}
                                </template>
                            </Column>
                            <Column header="Commentaire">
                                <template #body="slotProps">
                                    <div
                                        v-if="slotProps.data.commentaire"
                                        class="document-comment"
                                    >
                                        <i class="pi pi-comment"></i>
                                        <span>{{
                                            slotProps.data.commentaire
                                        }}</span>
                                    </div>
                                    <span v-else class="no-comment"
                                        >Pas encore évalué</span
                                    >
                                </template>
                            </Column>
                            <Column header="Actions">
                                <template #body="slotProps">
                                    <div class="document-actions">
                                        <Button
                                            icon="pi pi-download"
                                            text
                                            @click="
                                                downloadDocument(slotProps.data)
                                            "
                                            tooltip="Télécharger"
                                        />
                                        <Button
                                            v-if="!slotProps.data.commentaire"
                                            icon="pi pi-trash"
                                            text
                                            class="p-button-danger"
                                            @click="
                                                confirmDeleteDocument(
                                                    slotProps.data
                                                )
                                            "
                                            tooltip="Supprimer"
                                        />
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";
import axios from "axios";
import UserInfoHeader from "@/components/UserInfoHeader.vue";

// PrimeVue components
import Toast from "primevue/toast";
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import FileUpload from "primevue/fileupload";
import ProgressSpinner from "primevue/progressspinner";
import Card from "primevue/card";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import ConfirmDialog from "primevue/confirmdialog";

export default {
    name: "DocumentSubmissionView",
    components: {
        Toast,
        Dialog,
        Button,
        InputText,
        FileUpload,
        ProgressSpinner,
        Card,
        DataTable,
        Column,
        ConfirmDialog,
        UserInfoHeader
    },
    setup() {
        const toast = useToast();
        const confirm = useConfirm();

        // State
        const loading = ref(false);
        const uploading = ref(false);
        const documents = ref([]);
        const showUploadDialog = ref(false);
        const fileUploadRef = ref(null);
        const fileUploadError = ref(false);
        const validationErrors = ref({});
        const searchQuery = ref("");

        const uploadForm = reactive({
            title: "",
            file: null,
        });

        // Computed properties
        const filteredDocuments = computed(() => {
            if (!searchQuery.value) return documents.value;

            const query = searchQuery.value.toLowerCase();
            return documents.value.filter((doc) => {
                return doc.titre.toLowerCase().includes(query);
            });
        });

        // Load documents on mount
        onMounted(() => {
            loadDocuments();
        });

        // Methods

        // Handle search from header
        const handleHeaderSearch = (query) => {
            searchQuery.value = query;
        };

        // Load submitted documents
        const loadDocuments = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/etudiant/documents-evaluation"
                );
                documents.value = response;
            } catch (error) {
                handleError(error, "Erreur lors du chargement des documents");
            } finally {
                loading.value = false;
            }
        };

        // Open document upload dialog
        const openUploadDialog = () => {
            resetUploadForm();
            showUploadDialog.value = true;
        };

        // Close document upload dialog
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
            } else if (uploadForm.title.length < 3) {
                validationErrors.value.title =
                    "Le titre doit contenir au moins 3 caractères";
            }

            return Object.keys(validationErrors.value).length === 0;
        };

        // Submit document
        async function submitDocument() {
            fileUploadError.value = !uploadForm.file;

            if (!validateForm() || fileUploadError.value) return;

            uploading.value = true;

            try {
                const formData = new FormData();
                formData.append("file", uploadForm.file);
                formData.append("titre", uploadForm.title);

                // Show an upload progress toast
                toast.add({
                    severity: "info",
                    summary: "Téléchargement en cours",
                    detail: "Veuillez patienter...",
                    life: 2000,
                });

                // Upload the file
                await ApiService.uploadFiles(
                    "/etudiant/documents-evaluation/upload",
                    formData
                );

                // Close dialog FIRST before reloading data
                showUploadDialog.value = false;

                // Success notification
                toast.add({
                    severity: "success",
                    summary: "Document soumis",
                    detail: "Votre document a été soumis avec succès",
                    life: 3000,
                });

                // Reset form and reload documents
                resetUploadForm();
                await loadDocuments();
            } catch (error) {
                handleError(error, "Erreur lors de la soumission du document");
            } finally {
                uploading.value = false;
            }
        }

        // Download document
        function downloadDocument(doc) {
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
                }/etudiant/documents-evaluation/download/${doc.id}`,
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

                    // If no filename from header, use doc title or default with proper extension
                    if (!filename) {
                        filename = doc.titre || "document";

                        // Ensure proper extension based on content type
                        if (contentType) {
                            if (
                                contentType.includes("pdf") &&
                                !filename.toLowerCase().endsWith(".pdf")
                            ) {
                                filename += ".pdf";
                            } else if (
                                contentType.includes("png") &&
                                !filename.toLowerCase().endsWith(".png")
                            ) {
                                filename += ".png";
                            } else if (
                                contentType.includes("jpeg") &&
                                !filename.toLowerCase().endsWith(".jpg") &&
                                !filename.toLowerCase().endsWith(".jpeg")
                            ) {
                                filename += ".jpg";
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
        }
        // Confirm document deletion
        const confirmDeleteDocument = (document) => {
            confirm.require({
                message: "Êtes-vous sûr de vouloir supprimer ce document ?",
                header: "Confirmation de suppression",
                icon: "pi pi-exclamation-triangle",
                acceptClass: "p-button-danger",
                accept: () => deleteDocument(document),
                reject: () => {},
            });
        };

        // Delete document
        const deleteDocument = async (document) => {
            try {
                await ApiService.delete(
                    `/etudiant/documents-evaluation/${document.id}`
                );

                toast.add({
                    severity: "success",
                    summary: "Document supprimé",
                    detail: "Le document a été supprimé avec succès",
                    life: 3000,
                });

                loadDocuments();
            } catch (error) {
                handleError(error, "Erreur lors de la suppression du document");
            }
        };

        // Format date
        const formatDate = (dateString) => {
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
            documents,
            showUploadDialog,
            uploadForm,
            fileUploadRef,
            fileUploadError,
            validationErrors,
            searchQuery,

            // Computed
            filteredDocuments,

            // Methods
            loadDocuments,
            openUploadDialog,
            closeUploadDialog,
            onFileSelect,
            onFileClear,
            submitDocument,
            downloadDocument,
            confirmDeleteDocument,
            formatDate,
            handleHeaderSearch
        };
    },
};
</script>

<style scoped>
.document-submission-view {
    margin: 0 auto;
}

.view-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
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

.required-marker {
    color: var(--red-500);
    margin-left: 0.25rem;
}

.submission-form {
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

.empty-documents {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 1rem;
    text-align: center;
}

.empty-icon {
    font-size: 3rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.empty-documents h3 {
    margin: 0 0 0.5rem;
    color: var(--text-color);
}

.empty-documents p {
    margin: 0;
    color: var(--text-color-secondary);
}

.document-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}

.document-comment {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.document-comment i {
    color: var(--primary-color);
}

.no-comment {
    color: var(--text-color-secondary);
    font-style: italic;
}

/* Responsive styles */
@media (max-width: 768px) {
    .header-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .action-buttons {
        width: 100%;
    }

    .action-buttons .p-button {
        width: 100%;
    }
}
</style>