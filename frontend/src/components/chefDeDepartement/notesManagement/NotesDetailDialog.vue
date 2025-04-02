<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        :header="getDialogHeader()"
        :style="{ width: '50vw' }"
        :modal="true"
        :closable="true"
        :closeOnEscape="true"
    >
        <div v-if="loading" class="flex justify-content-center">
            <ProgressSpinner />
        </div>
        <div
            v-else-if="!hasAnyNoteDetails()"
            class="flex justify-content-center"
        >
            <p>Aucun détail disponible pour cet étudiant.</p>
        </div>
        <div v-else class="note-details-container">
            <!-- Encadrant Note Details -->
            <div v-if="noteDetail?.noteEncadrantDetail" class="detail-section">
                <h3>Évaluation de l'Encadrant</h3>
                <div class="flex flex-wrap">
                    <div class="detail-item flex-1">
                        <label>Technique:</label>
                        <span
                            >{{
                                noteDetail.noteEncadrantDetail.technicalScore
                            }}/20</span
                        >
                    </div>
                    <div class="detail-item flex-1">
                        <label>Rapport:</label>
                        <span
                            >{{
                                noteDetail.noteEncadrantDetail.reportScore
                            }}/20</span
                        >
                    </div>
                    <div class="detail-item flex-1">
                        <label>Progression:</label>
                        <span
                            >{{
                                noteDetail.noteEncadrantDetail.progressScore
                            }}/20</span
                        >
                    </div>
                    <div class="detail-item flex-1">
                        <label>Professionnalisme:</label>
                        <span
                            >{{
                                noteDetail.noteEncadrantDetail
                                    .professionalismScore
                            }}/20</span
                        >
                    </div>
                </div>
                <div
                    class="detail-item mt-3"
                    v-if="noteDetail.noteEncadrantDetail.commentaire"
                >
                    <label>Commentaire:</label>
                    <p>{{ noteDetail.noteEncadrantDetail.commentaire }}</p>
                </div>
                <div class="evaluator-info mt-2">
                    <small
                        >Évalué par:
                        {{ noteDetail.noteEncadrantDetail.encadrantPrenom }}
                        {{ noteDetail.noteEncadrantDetail.encadrantNom }}</small
                    >
                </div>
            </div>

            <!-- Soutenance Note Details -->
            <div v-if="noteDetail?.noteSoutenanceDetail" class="detail-section">
                <h3>Évaluation de la Soutenance</h3>

                <!-- Individual Jury Evaluations -->
                <div
                    v-if="
                        noteDetail.noteSoutenanceDetail.juryEvaluations &&
                        noteDetail.noteSoutenanceDetail.juryEvaluations.length >
                            0
                    "
                >
                    <div
                        v-for="(jury, index) in noteDetail.noteSoutenanceDetail
                            .juryEvaluations"
                        :key="index"
                        class="jury-evaluation mb-3"
                    >
                        <h4>
                            Évaluation du Jury {{ index + 1 }}:
                            {{ jury.juryNom }} {{ jury.juryPrenom }}
                        </h4>
                        <div class="flex flex-wrap">
                            <div class="detail-item flex-1">
                                <label>Présentation:</label>
                                <span>{{ jury.presentationScore }}/20</span>
                            </div>
                            <div class="detail-item flex-1">
                                <label>Questions/Réponses:</label>
                                <span>{{ jury.qaScore }}/20</span>
                            </div>
                            <div class="detail-item flex-1">
                                <label>Gestion du temps:</label>
                                <span>{{ jury.timeManagementScore }}/20</span>
                            </div>
                        </div>
                        <div class="detail-item mt-2" v-if="jury.commentaire">
                            <label>Commentaire:</label>
                            <p>{{ jury.commentaire }}</p>
                        </div>
                    </div>
                </div>

                <!-- Fallback if individual evaluations aren't available -->
                <div v-else>
                    <div class="flex flex-wrap">
                        <div class="detail-item flex-1">
                            <label>Présentation:</label>
                            <span
                                >{{
                                    noteDetail.noteSoutenanceDetail
                                        .presentationScore
                                }}/20</span
                            >
                        </div>
                        <div class="detail-item flex-1">
                            <label>Questions/Réponses:</label>
                            <span
                                >{{
                                    noteDetail.noteSoutenanceDetail.qaScore
                                }}/20</span
                            >
                        </div>
                        <div class="detail-item flex-1">
                            <label>Gestion du temps:</label>
                            <span
                                >{{
                                    noteDetail.noteSoutenanceDetail
                                        .timeManagementScore
                                }}/20</span
                            >
                        </div>
                    </div>
                    <div
                        class="detail-item mt-3"
                        v-if="noteDetail.noteSoutenanceDetail.commentaire"
                    >
                        <label>Commentaire:</label>
                        <p>{{ noteDetail.noteSoutenanceDetail.commentaire }}</p>
                    </div>
                    <div class="evaluator-info mt-2">
                        <small
                            >Évalué par
                            {{
                                noteDetail.noteSoutenanceDetail.juryCount
                            }}
                            membre(s) du jury</small
                        >
                    </div>
                </div>
            </div>

            <!-- Rapport Note Details -->
            <div v-if="noteDetail?.noteRapportDetail" class="detail-section">
                <h3>Évaluation du Rapport</h3>
                <div class="detail-item mb-2">
                    <label>Titre du rapport:</label>
                    <span>{{ noteDetail.noteRapportDetail.titre }}</span>
                </div>
                <div class="flex flex-wrap">
                    <div class="detail-item flex-1">
                        <label>Contenu technique:</label>
                        <span
                            >{{
                                noteDetail.noteRapportDetail.technicalScore
                            }}/20</span
                        >
                    </div>
                    <div class="detail-item flex-1">
                        <label>Structure:</label>
                        <span
                            >{{
                                noteDetail.noteRapportDetail.structureScore
                            }}/20</span
                        >
                    </div>
                    <div class="detail-item flex-1">
                        <label>Originalité:</label>
                        <span
                            >{{
                                noteDetail.noteRapportDetail.originalityScore
                            }}/20</span
                        >
                    </div>
                </div>
                <div
                    class="detail-item mt-3"
                    v-if="noteDetail.noteRapportDetail.commentaire"
                >
                    <label>Commentaire:</label>
                    <p>{{ noteDetail.noteRapportDetail.commentaire }}</p>
                </div>
                <div class="evaluator-info mt-2">
                    <small>
                        Évalué par
                        {{
                            noteDetail.noteRapportDetail.evaluateurCount
                        }}
                        évaluateur(s) · Date de soumission:
                        {{
                            formatDate(
                                noteDetail.noteRapportDetail.dateSoumission
                            )
                        }}
                    </small>
                </div>
            </div>
        </div>
    </Dialog>
</template>

<script setup>
import { ref, watch } from "vue";
import Dialog from "primevue/dialog";
import ProgressSpinner from "primevue/progressspinner";
import ApiService from "@/services/ApiService";

const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
    etudiantId: {
        type: Number,
        default: null,
    },
    etudiantNom: {
        type: String,
        default: "",
    },
    etudiantPrenom: {
        type: String,
        default: "",
    },
});

const emit = defineEmits(["update:visible"]);

const loading = ref(false);
const noteDetail = ref(null);

// Watch for changes in visibility
watch(
    () => props.visible,
    (newValue) => {
        if (newValue && props.etudiantId) {
            fetchNoteDetails();
        }
    }
);

// Watch for changes in student ID
watch(
    () => props.etudiantId,
    (newValue) => {
        if (props.visible && newValue) {
            fetchNoteDetails();
        }
    }
);

// Fetch note details when dialog is opened
async function fetchNoteDetails() {
    if (!props.etudiantId) return;

    loading.value = true;
    noteDetail.value = null;

    try {
        const response = await ApiService.get(
            `/chef_de_departement/notes/student/${props.etudiantId}/details`
        );
        console.log("Note details response:", response);
        noteDetail.value = response;
    } catch (error) {
        console.error("Error fetching note details:", error);
    } finally {
        loading.value = false;
    }
}

// Check if any note details are available
function hasAnyNoteDetails() {
    return (
        noteDetail.value &&
        ((noteDetail.value.noteEncadrantDetail &&
            Object.keys(noteDetail.value.noteEncadrantDetail).length > 0) ||
            (noteDetail.value.noteSoutenanceDetail &&
                Object.keys(noteDetail.value.noteSoutenanceDetail).length >
                    0) ||
            (noteDetail.value.noteRapportDetail &&
                Object.keys(noteDetail.value.noteRapportDetail).length > 0))
    );
}

// Format date for display
function formatDate(dateStr) {
    if (!dateStr) return "";
    try {
        const date = new Date(dateStr);
        return new Intl.DateTimeFormat("fr-FR", {
            dateStyle: "medium",
            timeStyle: "short",
        }).format(date);
    } catch (e) {
        console.error("Error formatting date:", e);
        return dateStr;
    }
}

// Get dialog header
function getDialogHeader() {
    return `Détails des notes de ${props.etudiantPrenom} ${props.etudiantNom}`;
}
</script>

<style scoped>
.note-details-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.detail-section {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.detail-section h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    font-size: 1.25rem;
    color: var(--primary-color);
    font-weight: 600;
}

.detail-item {
    margin-bottom: 0.5rem;
}

.detail-item label {
    font-weight: 600;
    margin-right: 0.5rem;
    display: block;
}

.evaluator-info {
    color: var(--text-color-secondary);
    font-style: italic;
}

.dark-mode .detail-section {
    background-color: var(--surface-overlay);
}

.jury-evaluation {
    border-left: 3px solid var(--primary-color);
    padding-left: 1rem;
    margin-bottom: 1.5rem;
}

.jury-evaluation h4 {
    margin-top: 0;
    margin-bottom: 0.75rem;
    font-size: 1.1rem;
    color: var(--primary-color);
    font-weight: 500;
}

.jury-evaluation {
    border-left: 3px solid var(--primary-color);
    padding-left: 1rem;
    margin-bottom: 1.5rem;
}

.jury-evaluation h4 {
    margin-top: 0;
    margin-bottom: 0.75rem;
    font-size: 1.1rem;
    color: var(--primary-color);
    font-weight: 500;
}
</style>
