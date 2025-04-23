<template>
    <div class="encadrant-document-evaluation">
        <Toast />
        <ConfirmDialog />

        <!-- Header section -->
        <div class="header-section">
            <div class="title-filter-group">
                <h1 class="page-title">Évaluation des Documents</h1>
                <p class="page-subtitle">
                    Consultez et évaluez les documents soumis par vos binômes
                    étudiants
                </p>
            </div>

            <div class="action-buttons">
                <span class="p-input-icon-left search-input">
                    <i class="pi pi-search" />
                    <InputText
                        v-model="searchQuery"
                        placeholder="Rechercher un document..."
                    />
                </span>
            </div>
        </div>

        <!-- Main content -->
        <div class="content-container">
            <!-- Documents list -->
            <Card class="documents-card">
                <template #title>
                    <div class="card-header">
                        <h3>Documents à Évaluer</h3>
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
                        v-else-if="documents.length === 0"
                        class="empty-documents"
                    >
                        <i class="pi pi-file-o empty-icon"></i>
                        <h3>Aucun document à évaluer</h3>
                        <p>
                            Vous n'avez pas encore de documents soumis par vos
                            étudiants.
                        </p>
                    </div>

                    <DataTable
                        v-else
                        :value="filteredDocuments"
                        stripedRows
                        responsiveLayout="scroll"
                        class="documents-table"
                        :paginator="filteredDocuments.length > 10"
                        :rows="10"
                        v-model:selection="selectedDocument"
                        selectionMode="single"
                        dataKey="id"
                        @row-select="onDocumentSelect"
                        emptyMessage="Aucun document trouvé pour votre recherche"
                    >
                        <Column field="titre" header="Titre" sortable />
                        <Column header="Binôme">
                            <template #body="slotProps">
                                <div class="binome-info">
                                    <div class="student">
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.etudiant1Prenom
                                            }}
                                            {{
                                                slotProps.data.etudiant1Nom
                                            }}</span
                                        >
                                    </div>
                                    <div
                                        v-if="slotProps.data.etudiant2Nom"
                                        class="student"
                                    >
                                        <i class="pi pi-user"></i>
                                        <span
                                            >{{
                                                slotProps.data.etudiant2Prenom
                                            }}
                                            {{
                                                slotProps.data.etudiant2Nom
                                            }}</span
                                        >
                                    </div>
                                </div>
                            </template>
                        </Column>
                        <Column
                            header="Date de soumission"
                            sortable
                            sortField="dateSoumission"
                        >
                            <template #body="slotProps">
                                {{ formatDate(slotProps.data.dateSoumission) }}
                            </template>
                        </Column>
                        <Column header="Statut">
                            <template #body="slotProps">
                                <Tag
                                    :value="
                                        slotProps.data.commentaire
                                            ? 'Évalué'
                                            : 'Non évalué'
                                    "
                                    :severity="
                                        slotProps.data.commentaire
                                            ? 'success'
                                            : 'warning'
                                    "
                                />
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
                                        icon="pi pi-eye"
                                        text
                                        @click="
                                            openPreviewDialog(slotProps.data)
                                        "
                                        tooltip="Aperçu"
                                    />
                                </div>
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>

            <!-- Document details and evaluation form -->
            <div v-if="selectedDocument" class="eval-section">
                <Card class="document-details-card">
                    <template #title>
                        <div class="card-title">
                            <i
                                class="pi pi-file"
                                style="font-size: 1.5rem; margin-right: 0.5rem"
                            ></i>
                            Document: {{ selectedDocument.titre }}
                        </div>
                    </template>
                    <template #subtitle>
                        <div class="document-actions">
                            <Button
                                label="Télécharger"
                                icon="pi pi-download"
                                class="p-button-outlined p-button-primary"
                                @click="downloadDocument(selectedDocument)"
                            />
                            <Button
                                label="Prévisualiser"
                                icon="pi pi-eye"
                                class="p-button-outlined"
                                @click="openPreviewDialog(selectedDocument)"
                            />
                        </div>
                    </template>
                    <template #content>
                        <div class="document-info">
                            <div class="info-row">
                                <div class="info-label">Titre:</div>
                                <div class="info-value">
                                    {{ selectedDocument.titre }}
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Soumis par:</div>
                                <div class="info-value">
                                    {{ selectedDocument.etudiant1Prenom }}
                                    {{ selectedDocument.etudiant1Nom }}
                                    <span v-if="selectedDocument.etudiant2Nom">
                                        et
                                        {{ selectedDocument.etudiant2Prenom }}
                                        {{ selectedDocument.etudiant2Nom }}
                                    </span>
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">
                                    Date de soumission:
                                </div>
                                <div class="info-value">
                                    {{
                                        formatDateTime(
                                            selectedDocument.dateSoumission
                                        )
                                    }}
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Type de document:</div>
                                <div class="info-value">
                                    {{ getFileType(selectedDocument) }}
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Evaluation form -->
                <Card class="evaluation-card">
                    <template #title>
                        <div class="card-title">
                            <i
                                class="pi pi-pencil"
                                style="font-size: 1.5rem; margin-right: 0.5rem"
                            ></i>
                            Évaluation du Document
                        </div>
                    </template>
                    <template #content>
                        <div class="eval-form">
                            <div class="form-field">
                                <label for="commentaire">Commentaires</label>
                                <Textarea
                                    id="commentaire"
                                    v-model="evaluationForm.commentaire"
                                    rows="5"
                                    class="w-full"
                                    placeholder="Vos commentaires sur le document soumis"
                                    autoResize
                                />
                                <small
                                    >Fournissez un feedback détaillé sur le
                                    document pour aider les étudiants à
                                    améliorer leur travail.</small
                                >
                            </div>

                            <div class="form-actions">
                                <Button
                                    label="Soumettre l'évaluation"
                                    icon="pi pi-check"
                                    @click="submitEvaluation"
                                    :loading="submitting"
                                    :disabled="!evaluationForm.commentaire"
                                    class="p-button-primary"
                                />
                            </div>
                        </div>
                    </template>
                </Card>
            </div>

            <Card v-else class="eval-section info-card">
                <template #content>
                    <div class="info-message">
                        <i
                            class="pi pi-info-circle"
                            style="font-size: 2rem; color: var(--primary-color)"
                        ></i>
                        <h3>Sélectionnez un document pour l'évaluer</h3>
                        <p>
                            Choisissez un document dans la liste pour commencer
                            votre évaluation
                        </p>
                    </div>
                </template>
            </Card>
        </div>

        <!-- Document Preview Dialog -->
        <Dialog
            v-model:visible="showPreviewDialog"
            :modal="true"
            :style="{ width: '90vw', height: '90vh' }"
            :header="
                previewDocument ? previewDocument.titre : 'Aperçu du document'
            "
            :maximizable="true"
            @show="loadPreviewContent"
        >
            <div class="preview-dialog-content">
                <div v-if="previewLoading" class="preview-loading">
                    <ProgressSpinner />
                    <p>Chargement du document en cours...</p>
                </div>
                <div v-else-if="previewError" class="preview-error">
                    <i
                        class="pi pi-exclamation-triangle"
                        style="font-size: 3rem; color: var(--red-500)"
                    ></i>
                    <h3>Erreur de prévisualisation</h3>
                    <p>{{ previewError }}</p>
                    <Button
                        label="Télécharger plutôt"
                        icon="pi pi-download"
                        @click="downloadDocument(previewDocument)"
                        class="mt-3"
                    />
                </div>
                <div
                    v-else-if="
                        previewContentType &&
                        previewContentType.startsWith('image/')
                    "
                    class="image-preview"
                >
                    <img
                        :src="previewDataUrl"
                        :alt="previewDocument && previewDocument.titre"
                    />
                </div>
                <div
                    v-else-if="previewContentType === 'application/pdf'"
                    class="pdf-preview"
                >
                    <object
                        :data="previewDataUrl"
                        type="application/pdf"
                        width="100%"
                        height="100%"
                    >
                        <p>
                            Ce PDF ne peut pas être affiché.
                            <Button
                                label="Télécharger le PDF"
                                icon="pi pi-download"
                                @click="downloadDocument(previewDocument)"
                                class="mt-3"
                            />
                        </p>
                    </object>
                </div>
                <div v-else class="preview-not-available">
                    <i
                        class="pi pi-file-export"
                        style="
                            font-size: 3rem;
                            color: var(--text-color-secondary);
                        "
                    ></i>
                    <h3>Aperçu limité</h3>
                    <p>
                        Ce type de document ({{
                            previewDocument && getFileType(previewDocument)
                        }}) ne peut pas être prévisualisé directement.
                    </p>
                    <Button
                        label="Télécharger le document"
                        icon="pi pi-download"
                        @click="downloadDocument(previewDocument)"
                        class="mt-3"
                    />
                </div>
            </div>
        </Dialog>
    </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";
import Card from "primevue/card";
import Button from "primevue/button";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Textarea from "primevue/textarea";
import InputText from "primevue/inputtext";
import ProgressSpinner from "primevue/progressspinner";
import Tag from "primevue/tag";
import Dialog from "primevue/dialog";

export default {
    name: "EncadrantDocumentEvaluationView",
    components: {
        Toast,
        ConfirmDialog,
        Card,
        Button,
        DataTable,
        Column,
        Textarea,
        InputText,
        ProgressSpinner,
        Tag,
        Dialog,
    },
    setup() {
        const toast = useToast();
        const confirm = useConfirm();

        // State
        const loading = ref(false);
        const submitting = ref(false);
        const documents = ref([]);
        const selectedDocument = ref(null);
        const searchQuery = ref("");

        // Preview dialog state
        const showPreviewDialog = ref(false);
        const previewDocument = ref(null);
        const previewLoading = ref(false);
        const previewError = ref(null);
        const previewDataUrl = ref("");
        const previewContentType = ref("");

        // Blob URLs to revoke on unmount
        const blobUrls = ref([]);

        const evaluationForm = ref({
            commentaire: "",
        });

        // Computed
        const filteredDocuments = computed(() => {
            if (!searchQuery.value) {
                return documents.value;
            }

            const query = searchQuery.value.toLowerCase();
            return documents.value.filter((doc) => {
                return (
                    doc.titre.toLowerCase().includes(query) ||
                    `${doc.etudiant1Prenom} ${doc.etudiant1Nom}`
                        .toLowerCase()
                        .includes(query) ||
                    (doc.etudiant2Nom &&
                        `${doc.etudiant2Prenom} ${doc.etudiant2Nom}`
                            .toLowerCase()
                            .includes(query))
                );
            });
        });

        // Clean up blob URLs when component is destroyed
        onBeforeUnmount(() => {
            blobUrls.value.forEach((url) => {
                URL.revokeObjectURL(url);
            });
        });

        // Load documents on mount
        onMounted(() => {
            loadDocuments();
        });

        // Methods
        const loadDocuments = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/encadrant/documents-evaluation"
                );
                documents.value = response;
            } catch (error) {
                handleError(error, "Erreur lors du chargement des documents");
            } finally {
                loading.value = false;
            }
        };

        const onDocumentSelect = (event) => {
            selectedDocument.value = event.data;
            loadExistingEvaluation();
        };

        const openPreviewDialog = (doc) => {
            previewDocument.value = doc;
            previewLoading.value = true;
            previewError.value = null;
            previewDataUrl.value = "";
            previewContentType.value = "";
            showPreviewDialog.value = true;
        };

        const loadPreviewContent = () => {
            if (!previewDocument.value) return;

            previewLoading.value = true;
            previewError.value = null;

            const token =
                localStorage.getItem("token") ||
                sessionStorage.getItem("token");
            const baseUrl = import.meta.env.VITE_API_URL || "/api";

            // Change from download endpoint to preview endpoint
            const previewUrl = `${baseUrl}/encadrant/documents-evaluation/preview/${previewDocument.value.id}`;

            // For PDF documents, use direct iframe source instead of blob URL
            if (
                getFileExtension(
                    previewDocument.value.localisationDoc
                ).toLowerCase() === ".pdf"
            ) {
                previewDataUrl.value = previewUrl;
                previewContentType.value = "application/pdf";
                previewLoading.value = false;
                return;
            }

            fetch(previewUrl, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error(
                            `Erreur de prévisualisation: ${response.status} ${response.statusText}`
                        );
                    }

                    // Get content type
                    const contentType = response.headers.get("content-type");
                    previewContentType.value = contentType;

                    return response.blob();
                })
                .then((blob) => {
                    // Create a blob URL for the preview
                    const url = URL.createObjectURL(blob);

                    // Store for cleanup later
                    blobUrls.value.push(url);

                    // Set the data URL for preview
                    previewDataUrl.value = url;
                    previewLoading.value = false;
                })
                .catch((error) => {
                    console.error("Preview error:", error);
                    previewError.value =
                        error.message ||
                        "Impossible de prévisualiser le fichier";
                    previewLoading.value = false;
                });
        };

        // Add a helper function to get file extension
        const getFileExtension = (filePath) => {
            if (!filePath) return "";
            return filePath.substring(filePath.lastIndexOf("."));
        };

        const getFileType = (doc) => {
            const filePath = doc?.localisationDoc;
            if (!filePath) return "Document";

            const extension = filePath
                .substring(filePath.lastIndexOf("."))
                .toLowerCase();
            switch (extension) {
                case ".pdf":
                    return "Document PDF";
                case ".doc":
                    return "Document Word";
                case ".docx":
                    return "Document Word";
                case ".jpg":
                case ".jpeg":
                    return "Image JPEG";
                case ".png":
                    return "Image PNG";
                case ".gif":
                    return "Image GIF";
                case ".svg":
                    return "Image SVG";
                default:
                    return `Document ${extension}`;
            }
        };

        const downloadDocument = (doc) => {
            if (!doc) return;

            toast.add({
                severity: "info",
                summary: "Téléchargement",
                detail: "Préparation du téléchargement...",
                life: 2000,
            });

            // Get auth token for the request
            const token =
                localStorage.getItem("token") ||
                sessionStorage.getItem("token");
            const baseUrl = import.meta.env.VITE_API_URL || "/api";

            fetch(
                `${baseUrl}/encadrant/documents-evaluation/download/${doc.id}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            )
                .then((response) => {
                    if (!response.ok) {
                        throw new Error(
                            `Erreur de téléchargement: ${response.status} ${response.statusText}`
                        );
                    }

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

                    // If no filename from header, use document title or default
                    if (!filename) {
                        filename = doc.titre || "document";

                        // Add extension based on content type
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
                            } else if (
                                contentType.includes("jpeg") &&
                                !filename.toLowerCase().endsWith(".jpg")
                            ) {
                                filename += ".jpg";
                            } else if (
                                contentType.includes("png") &&
                                !filename.toLowerCase().endsWith(".png")
                            ) {
                                filename += ".png";
                            }
                        }
                    }

                    // Sanitize filename
                    filename = filename.replace(/[/\\?%*:|"<>]/g, "-");

                    // Return blob and filename for next step
                    return response.blob().then((blob) => {
                        return { blob, filename };
                    });
                })
                .then(({ blob, filename }) => {
                    // Create a blob URL and trigger download
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
                        detail:
                            error.message ||
                            "Impossible de télécharger le fichier",
                        life: 3000,
                    });
                });
        };

        const loadExistingEvaluation = () => {
            if (!selectedDocument.value) return;

            // Reset the form
            evaluationForm.value.commentaire = "";

            // If there's already a commentaire, load it
            if (selectedDocument.value.commentaire) {
                evaluationForm.value.commentaire =
                    selectedDocument.value.commentaire;
            }
        };

        const submitEvaluation = async () => {
            if (!selectedDocument.value) return;

            if (!evaluationForm.value.commentaire.trim()) {
                toast.add({
                    severity: "warn",
                    summary: "Champ requis",
                    detail: "Veuillez saisir un commentaire pour l'évaluation",
                    life: 3000,
                });
                return;
            }

            confirm.require({
                message:
                    "Êtes-vous sûr de vouloir soumettre cette évaluation ?",
                header: "Confirmation",
                icon: "pi pi-info-circle",
                acceptClass: "p-button-primary",
                accept: async () => {
                    submitting.value = true;
                    try {
                        await ApiService.post(
                            `/encadrant/documents-evaluation/evaluate/${selectedDocument.value.id}`,
                            {
                                commentaire: evaluationForm.value.commentaire,
                            }
                        );

                        // Update the document in the list with the new evaluation
                        const index = documents.value.findIndex(
                            (d) => d.id === selectedDocument.value.id
                        );
                        if (index !== -1) {
                            documents.value[index].commentaire =
                                evaluationForm.value.commentaire;
                        }

                        // Update the selected document too
                        selectedDocument.value.commentaire =
                            evaluationForm.value.commentaire;

                        toast.add({
                            severity: "success",
                            summary: "Évaluation soumise",
                            detail: "Votre évaluation a été enregistrée avec succès",
                            life: 3000,
                        });
                    } catch (error) {
                        handleError(
                            error,
                            "Erreur lors de la soumission de l'évaluation"
                        );
                    } finally {
                        submitting.value = false;
                    }
                },
            });
        };

        // Helper functions
        const formatDate = (dateString) => {
            if (!dateString) return "";
            const date = new Date(dateString);
            return new Intl.DateTimeFormat("fr-FR", {
                year: "numeric",
                month: "long",
                day: "numeric",
            }).format(date);
        };

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
            submitting,
            documents,
            selectedDocument,
            searchQuery,
            showPreviewDialog,
            previewDocument,
            previewLoading,
            previewError,
            previewDataUrl,
            previewContentType,
            evaluationForm,

            // Computed
            filteredDocuments,

            // Methods
            loadDocuments,
            onDocumentSelect,
            openPreviewDialog,
            loadPreviewContent,
            getFileType,
            downloadDocument,
            submitEvaluation,
            formatDate,
            formatDateTime,
        };
    },
};
</script>

<style scoped>
.encadrant-document-evaluation {
    margin: 0 auto;
}

.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
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

.search-input {
    width: 100%;
}

.content-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
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

.loading-container,
.preview-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 2rem;
}

.loading-container p,
.preview-loading p {
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

.binome-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.student {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.document-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
    flex-wrap: wrap;
}

.eval-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.card-title {
    display: flex;
    align-items: center;
}

.document-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.info-row {
    display: flex;
    gap: 0.5rem;
}

.info-label {
    font-weight: 600;
    min-width: 150px;
}

.eval-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-field label {
    font-weight: 600;
}

.form-field small {
    color: var(--text-color-secondary);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 1rem;
}

.info-card {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
}

.info-message {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 2rem;
}

.info-message h3 {
    margin-top: 1rem;
    margin-bottom: 0.5rem;
}

.info-message p {
    color: var(--text-color-secondary);
}

/* Preview Dialog Styles */
.preview-dialog-content {
    height: 80vh;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.preview-loading {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.preview-error,
.preview-not-available {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    height: 100%;
    padding: 2rem;
}

.preview-error h3,
.preview-not-available h3 {
    margin: 1rem 0 0.5rem;
}

.preview-error p,
.preview-not-available p {
    margin: 0 0 1rem;
    color: var(--text-color-secondary);
    max-width: 500px;
}

.image-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    width: 100%;
    background-color: var(--surface-ground);
    border-radius: 4px;
    overflow: hidden;
}

.image-preview img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.pdf-preview {
    height: 100%;
    width: 100%;
}

.pdf-preview iframe {
    border: none;
    background-color: white;
}

.mt-3 {
    margin-top: 1rem;
}

/* Responsive styles */
@media (max-width: 992px) {
    .content-container {
        grid-template-columns: 1fr;
    }

    .header-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .action-buttons {
        width: 100%;
    }

    .eval-section {
        margin-top: 1rem;
    }

    .info-row {
        flex-direction: column;
    }

    .info-label {
        min-width: auto;
    }
}
</style>
