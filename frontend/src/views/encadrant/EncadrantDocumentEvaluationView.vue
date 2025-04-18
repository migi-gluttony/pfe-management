<template>
    <div class="encadrant-document-evaluation">
        <Toast />
        <ConfirmDialog />

        <!-- Header section -->
        <div class="header-section">
            <div class="title-filter-group">
                <h1 class="page-title">Évaluation des Documents</h1>
                <p class="page-subtitle">
                    Consultez et évaluez les documents soumis par vos binômes étudiants
                </p>
            </div>

            <div class="action-buttons">
                <span class="p-input-icon-left search-input">
                    <i class="pi pi-search" />
                    <InputText v-model="searchQuery" placeholder="Rechercher un document..." />
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

                    <div v-else-if="documents.length === 0" class="empty-documents">
                        <i class="pi pi-file-o empty-icon"></i>
                        <h3>Aucun document à évaluer</h3>
                        <p>
                            Vous n'avez pas encore de documents soumis par vos étudiants.
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
                                        <span>{{ slotProps.data.etudiant1Prenom }} {{ slotProps.data.etudiant1Nom }}</span>
                                    </div>
                                    <div v-if="slotProps.data.etudiant2Nom" class="student">
                                        <i class="pi pi-user"></i>
                                        <span>{{ slotProps.data.etudiant2Prenom }} {{ slotProps.data.etudiant2Nom }}</span>
                                    </div>
                                </div>
                            </template>
                        </Column>
                        <Column header="Date de soumission" sortable sortField="dateSoumission">
                            <template #body="slotProps">
                                {{ formatDate(slotProps.data.dateSoumission) }}
                            </template>
                        </Column>
                        <Column header="Statut">
                            <template #body="slotProps">
                                <Tag 
                                    :value="slotProps.data.commentaire ? 'Évalué' : 'Non évalué'" 
                                    :severity="slotProps.data.commentaire ? 'success' : 'warning'" 
                                />
                            </template>
                        </Column>
                        <Column header="Actions">
                            <template #body="slotProps">
                                <div class="document-actions">
                                    <Button
                                        icon="pi pi-download"
                                        text
                                        @click="downloadDocument(slotProps.data)"
                                        tooltip="Télécharger"
                                    />
                                    <Button
                                        icon="pi pi-eye"
                                        text
                                        @click="openDocumentPreview(slotProps.data)"
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
                <Card class="document-preview-card">
                    <template #title>
                        <div class="card-title">
                            <i class="pi pi-file" style="font-size: 1.5rem; margin-right: 0.5rem"></i>
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
                                :label="showDocPreview ? 'Masquer l\'aperçu' : 'Afficher l\'aperçu'"
                                :icon="showDocPreview ? 'pi pi-eye-slash' : 'pi pi-eye'"
                                @click="toggleDocPreview"
                            />
                            <Button
                                label="Voir en plein écran"
                                icon="pi pi-window-maximize"
                                class="p-button-outlined"
                                @click="openPreviewInNewTab"
                            />
                        </div>
                    </template>
                    <template #content>
                        <div v-if="showDocPreview" class="document-preview-container">
                            <iframe
                                v-if="isPdfFile(selectedDocument)"
                                :src="getPreviewUrl(selectedDocument)"
                                width="100%"
                                height="500"
                                frameborder="0"
                                title="Document Preview"
                                sandbox="allow-same-origin allow-scripts"
                            ></iframe>
                            <div v-else class="preview-not-available">
                                <i class="pi pi-file-export" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
                                <h3>Aperçu limité</h3>
                                <p>Ce type de document ({{ getFileType(selectedDocument) }}) peut nécessiter un téléchargement pour être visualisé correctement.</p>
                                <Button
                                    label="Télécharger le document"
                                    icon="pi pi-download"
                                    @click="downloadDocument(selectedDocument)"
                                    class="mt-3"
                                />
                            </div>
                        </div>
                        <div v-else class="document-info">
                            <div class="info-row">
                                <div class="info-label">Titre:</div>
                                <div class="info-value">{{ selectedDocument.titre }}</div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Soumis par:</div>
                                <div class="info-value">
                                    {{ selectedDocument.etudiant1Prenom }} {{ selectedDocument.etudiant1Nom }}
                                    <span v-if="selectedDocument.etudiant2Nom">
                                        et {{ selectedDocument.etudiant2Prenom }} {{ selectedDocument.etudiant2Nom }}
                                    </span>
                                </div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Date de soumission:</div>
                                <div class="info-value">{{ formatDateTime(selectedDocument.dateSoumission) }}</div>
                            </div>
                            <div class="info-row">
                                <div class="info-label">Type de document:</div>
                                <div class="info-value">{{ getFileType(selectedDocument) }}</div>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Evaluation form -->
                <Card class="evaluation-card">
                    <template #title>
                        <div class="card-title">
                            <i class="pi pi-pencil" style="font-size: 1.5rem; margin-right: 0.5rem"></i>
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
                                <small>Fournissez un feedback détaillé sur le document pour aider les étudiants à améliorer leur travail.</small>
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
                        <i class="pi pi-info-circle" style="font-size: 2rem; color: var(--primary-color)"></i>
                        <h3>Sélectionnez un document pour l'évaluer</h3>
                        <p>Choisissez un document dans la liste pour commencer votre évaluation</p>
                    </div>
                </template>
            </Card>
        </div>

        <!-- Document Dialog for preview -->
        <Dialog
            v-model:visible="showDocDialog"
            :modal="true"
            :style="{ width: '90vw', height: '90vh' }"
            :header="selectedDocument ? selectedDocument.titre : 'Aperçu du document'"
            :maximizable="true"
        >
            <div v-if="selectedDocument" class="dialog-content">
                <iframe
                    v-if="isPdfFile(selectedDocument)"
                    :src="getPreviewUrl(selectedDocument)"
                    width="100%"
                    height="100%"
                    frameborder="0"
                    title="Document Viewer"
                    sandbox="allow-same-origin allow-scripts"
                ></iframe>
                <div v-else class="preview-not-available">
                    <i class="pi pi-file-export" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
                    <h3>Aperçu limité</h3>
                    <p>Ce type de document ({{ getFileType(selectedDocument) }}) peut nécessiter un téléchargement pour être visualisé correctement.</p>
                    <Button
                        label="Télécharger le document"
                        icon="pi pi-download"
                        @click="downloadDocument(selectedDocument)"
                        class="mt-3"
                    />
                </div>
            </div>
        </Dialog>
    </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import ApiService from '@/services/ApiService';

// PrimeVue components
import Toast from 'primevue/toast';
import ConfirmDialog from 'primevue/confirmdialog';
import Card from 'primevue/card';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import ProgressSpinner from 'primevue/progressspinner';
import Tag from 'primevue/tag';
import Dialog from 'primevue/dialog';

export default {
    name: 'EncadrantDocumentEvaluationView',
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
        Dialog
    },
    setup() {
        const toast = useToast();
        const confirm = useConfirm();
        
        // State
        const loading = ref(false);
        const submitting = ref(false);
        const documents = ref([]);
        const selectedDocument = ref(null);
        const searchQuery = ref('');
        const showDocPreview = ref(false);
        const showDocDialog = ref(false);
        
        const evaluationForm = ref({
            commentaire: ''
        });
        
        // Computed
        const filteredDocuments = computed(() => {
            if (!searchQuery.value) {
                return documents.value;
            }
            
            const query = searchQuery.value.toLowerCase();
            return documents.value.filter(doc => {
                return doc.titre.toLowerCase().includes(query) ||
                    `${doc.etudiant1Prenom} ${doc.etudiant1Nom}`.toLowerCase().includes(query) ||
                    (doc.etudiant2Nom && `${doc.etudiant2Prenom} ${doc.etudiant2Nom}`.toLowerCase().includes(query));
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
                const response = await ApiService.get('/encadrant/documents-evaluation');
                documents.value = response;
            } catch (error) {
                handleError(error, 'Erreur lors du chargement des documents');
            } finally {
                loading.value = false;
            }
        };
        
        const onDocumentSelect = (event) => {
            selectedDocument.value = event.data;
            showDocPreview.value = false;
            loadExistingEvaluation();
        };
        
        const openDocumentPreview = (document) => {
            selectedDocument.value = document;
            showDocPreview.value = true;
        };
        
        const toggleDocPreview = () => {
            showDocPreview.value = !showDocPreview.value;
        };
        
        const getPreviewUrl = (document) => {
            const baseUrl = import.meta.env.VITE_API_URL || '/api';
            return `${baseUrl}/encadrant/documents-evaluation/preview/${document.id}`;
        };
        
        const openPreviewInNewTab = () => {
            if (!selectedDocument.value) return;
            
            if (isPdfFile(selectedDocument.value)) {
                const previewUrl = getPreviewUrl(selectedDocument.value);
                window.open(previewUrl, '_blank');
            } else {
                showDocDialog.value = true;
            }
        };
        
        const isPdfFile = (document) => {
            const filePath = document.localisationDoc;
            return filePath && filePath.toLowerCase().endsWith('.pdf');
        };
        
        const getFileType = (document) => {
            const filePath = document.localisationDoc;
            if (!filePath) return 'Document';
            
            const extension = filePath.substring(filePath.lastIndexOf('.')).toLowerCase();
            switch (extension) {
                case '.pdf': return 'Document PDF';
                case '.doc': return 'Document Word';
                case '.docx': return 'Document Word';
                case '.jpg':
                case '.jpeg': return 'Image JPEG';
                case '.png': return 'Image PNG';
                default: return `Document ${extension}`;
            }
        };

        const downloadDocument = (document) => {
            toast.add({
                severity: 'info',
                summary: 'Téléchargement',
                detail: 'Préparation du téléchargement...',
                life: 2000,
            });

            // Get auth token for the request
            const token = localStorage.getItem('token') || sessionStorage.getItem('token');
            const baseUrl = import.meta.env.VITE_API_URL || '/api';
            
            // Use window.fetch API instead of axios to handle the download properly
            fetch(`${baseUrl}/encadrant/documents-evaluation/download/${document.id}`, {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`Erreur de téléchargement: ${response.status} ${response.statusText}`);
                }

                // Get content type to determine extension
                const contentType = response.headers.get('content-type');

                // Try to get filename from Content-Disposition header
                let filename = '';
                const disposition = response.headers.get('content-disposition');
                if (disposition && disposition.includes('filename=')) {
                    const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                    const matches = filenameRegex.exec(disposition);
                    if (matches && matches[1]) {
                        filename = matches[1].replace(/['"]/g, '');
                    }
                }

                // If no filename from header, use document title or default
                if (!filename) {
                    filename = document.titre || 'document';

                    // Add extension based on content type
                    if (contentType) {
                        if (contentType.includes('pdf') && !filename.toLowerCase().endsWith('.pdf')) {
                            filename += '.pdf';
                        } else if (contentType.includes('msword') && !filename.toLowerCase().endsWith('.doc')) {
                            filename += '.doc';
                        } else if (contentType.includes('wordprocessingml') && !filename.toLowerCase().endsWith('.docx')) {
                            filename += '.docx';
                        } else if (contentType.includes('jpeg') && !filename.toLowerCase().endsWith('.jpg')) {
                            filename += '.jpg';
                        } else if (contentType.includes('png') && !filename.toLowerCase().endsWith('.png')) {
                            filename += '.png';
                        }
                    }
                }

                // Sanitize filename
                filename = filename.replace(/[/\\?%*:|"<>]/g, '-');

                return response.blob().then(blob => ({ blob, filename }));
            })
            .then(({ blob, filename }) => {
                // Create a blob URL and trigger download
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                
                // Clean up
                setTimeout(() => {
                    document.body.removeChild(a);
                    window.URL.revokeObjectURL(url);
                }, 100);

                toast.add({
                    severity: 'success',
                    summary: 'Téléchargement réussi',
                    detail: `Fichier "${filename}" téléchargé avec succès`,
                    life: 3000,
                });
            })
            .catch((error) => {
                console.error('Download error:', error);
                toast.add({
                    severity: 'error',
                    summary: 'Erreur de téléchargement',
                    detail: error.message || 'Impossible de télécharger le fichier',
                    life: 3000,
                });
            });
        };
        
        const loadExistingEvaluation = () => {
            if (!selectedDocument.value) return;
            
            // Reset the form
            evaluationForm.value.commentaire = '';
            
            // If there's already a commentaire, load it
            if (selectedDocument.value.commentaire) {
                evaluationForm.value.commentaire = selectedDocument.value.commentaire;
            }
        };
        
        const submitEvaluation = async () => {
            if (!selectedDocument.value) return;
            
            if (!evaluationForm.value.commentaire.trim()) {
                toast.add({
                    severity: 'warn',
                    summary: 'Champ requis',
                    detail: 'Veuillez saisir un commentaire pour l\'évaluation',
                    life: 3000
                });
                return;
            }
            
            confirm.require({
                message: 'Êtes-vous sûr de vouloir soumettre cette évaluation ?',
                header: 'Confirmation',
                icon: 'pi pi-info-circle',
                acceptClass: 'p-button-primary',
                accept: async () => {
                    submitting.value = true;
                    try {
                        await ApiService.post(`/encadrant/documents-evaluation/evaluate/${selectedDocument.value.id}`, {
                            commentaire: evaluationForm.value.commentaire
                        });
                        
                        // Update the document in the list with the new evaluation
                        const index = documents.value.findIndex(d => d.id === selectedDocument.value.id);
                        if (index !== -1) {
                            documents.value[index].commentaire = evaluationForm.value.commentaire;
                        }
                        
                        // Update the selected document too
                        selectedDocument.value.commentaire = evaluationForm.value.commentaire;
                        
                        toast.add({
                            severity: 'success',
                            summary: 'Évaluation soumise',
                            detail: 'Votre évaluation a été enregistrée avec succès',
                            life: 3000
                        });
                    } catch (error) {
                        handleError(error, 'Erreur lors de la soumission de l\'évaluation');
                    } finally {
                        submitting.value = false;
                    }
                }
            });
        };
        
        // Helper functions
        const formatDate = (dateString) => {
            if (!dateString) return '';
            const date = new Date(dateString);
            return new Intl.DateTimeFormat('fr-FR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            }).format(date);
        };
        
        const formatDateTime = (dateString) => {
            if (!dateString) return '';
            const date = new Date(dateString);
            return new Intl.DateTimeFormat('fr-FR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            }).format(date);
        };
        
        const handleError = (error, defaultMessage) => {
            console.error(defaultMessage, error);
            let message = defaultMessage;
            
            if (error.message) {
                message = error.message;
            } else if (error.response && error.response.data && error.response.data.message) {
                message = error.response.data.message;
            }
            
            toast.add({
                severity: 'error',
                summary: 'Erreur',
                detail: message,
                life: 5000
            });
        };
        
        return {
            // State
            loading,
            submitting,
            documents,
            selectedDocument,
            searchQuery,
            showDocPreview,
            showDocDialog,
            evaluationForm,
            
            // Computed
            filteredDocuments,
            
            // Methods
            loadDocuments,
            onDocumentSelect,
            openDocumentPreview,
            toggleDocPreview,
            getPreviewUrl,
            openPreviewInNewTab,
            isPdfFile,
            getFileType,
            downloadDocument,
            submitEvaluation,
            formatDate,
            formatDateTime
        };
    }
};
</script>

<style scoped>
.encadrant-document-evaluation {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
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

.document-preview-container {
    width: 100%;
    height: 500px;
    border: 1px solid var(--surface-border);
    border-radius: 4px;
    overflow: hidden;
}

.preview-not-available {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
    text-align: center;
    padding: 1rem;
    background-color: var(--surface-hover);
    border-radius: 4px;
}

.preview-not-available h3 {
    margin: 1rem 0 0.5rem;
}

.preview-not-available p {
    margin: 0;
    color: var(--text-color-secondary);
    max-width: 400px;
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

.dialog-content {
    height: 100%;
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