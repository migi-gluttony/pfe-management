<template>
    <Card class="table-card no-print">
        <template #content>
            <DataTable
                :value="filteredNotes"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredNotes.length > 10"
                :rows="10"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 20, 50]"
                currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} étudiants"
                emptyMessage="Aucune note trouvée"
            >
                <Column
                    field="etudiant.nom"
                    header="Nom"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.etudiant.nom }}
                    </template>
                </Column>
                <Column
                    field="etudiant.prenom"
                    header="Prénom"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.etudiant.prenom }}
                    </template>
                </Column>
                <Column
                    field="etudiant.cne"
                    header="CNE"
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.etudiant.cne || "-" }}
                    </template>
                </Column>
                <Column
                    field="noteRapport"
                    header="Note Rapport"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <span
                            :class="getGradeClass(slotProps.data.noteRapport)"
                        >
                            {{ formatGrade(slotProps.data.noteRapport) }}
                        </span>
                    </template>
                </Column>
                <Column
                    field="noteSoutenance"
                    header="Note Soutenance"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <span
                            :class="
                                getGradeClass(slotProps.data.noteSoutenance)
                            "
                        >
                            {{ formatGrade(slotProps.data.noteSoutenance) }}
                        </span>
                    </template>
                </Column>
                <Column
                    field="noteEncadrant"
                    header="Note Encadrant"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <span
                            :class="getGradeClass(slotProps.data.noteEncadrant)"
                        >
                            {{ formatGrade(slotProps.data.noteEncadrant) }}
                        </span>
                    </template>
                </Column>
                <Column
                    field="noteFinale"
                    header="Note Finale"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <span
                            v-if="canShowFinalGrade(slotProps.data)"
                            :class="
                                getGradeClass(
                                    calculateFinalGrade(slotProps.data)
                                )
                            "
                        >
                            <strong>{{
                                formatGrade(calculateFinalGrade(slotProps.data))
                            }}</strong>
                        </span>
                        <span v-else class="incomplete-grade">Incomplet</span>
                    </template>
                </Column>
                <Column
                    field="mention"
                    header="Mention"
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <Tag
                            v-if="canShowFinalGrade(slotProps.data)"
                            :severity="
                                getMentionSeverity(
                                    getMention(
                                        calculateFinalGrade(slotProps.data)
                                    )
                                )
                            "
                            :value="
                                getMention(calculateFinalGrade(slotProps.data))
                            "
                        ></Tag>
                        <span v-else>-</span>
                    </template>
                </Column>
                <!-- New column for details button -->
                <Column
                    header="Détails"
                    style="min-width: 6rem; text-align: center"
                >
                    <template #body="slotProps">
                        <Button
                            icon="pi pi-search"
                            class="p-button-rounded p-button-info p-button-sm"
                            @click="showDetails(slotProps.data)"
                            :disabled="!hasAnyNotes(slotProps.data)"
                            v-tooltip="'Voir les détails des notes'"
                        />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>

    <!-- Note details dialog -->
    <NotesDetailDialog
        v-model:visible="detailsVisible"
        :etudiantId="selectedStudent.id"
        :etudiantNom="selectedStudent.nom"
        :etudiantPrenom="selectedStudent.prenom"
    />
</template>

<script setup>
import { computed, ref } from "vue";
import Card from "primevue/card";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Tag from "primevue/tag";
import Button from "primevue/button";
import NotesDetailDialog from "./NotesDetailDialog.vue";

const props = defineProps({
    notes: {
        type: Array,
        default: () => [],
    },
    selectedFiliere: {
        type: [Number, String, null],
        default: null,
    },
    searchQuery: {
        type: String,
        default: "",
    },
    loading: {
        type: Boolean,
        default: false,
    },
    pourcentages: {
        type: Object,
        default: () => ({
            pourcentageRapport: 40,
            pourcentageSoutenance: 40,
            pourcentageEncadrant: 20,
        }),
    },
});

// State for details dialog
const detailsVisible = ref(false);
const selectedStudent = ref({
    id: null,
    nom: "",
    prenom: "",
});

// Computed filtered notes
const filteredNotes = computed(() => {
    const query = props.searchQuery.toLowerCase();
    let filtered = [...props.notes];

    if (props.selectedFiliere) {
        filtered = filtered.filter(
            (note) => note.filiereId === props.selectedFiliere
        );
    }

    if (query) {
        filtered = filtered.filter(
            (note) =>
                note.etudiant.nom.toLowerCase().includes(query) ||
                note.etudiant.prenom.toLowerCase().includes(query) ||
                (note.etudiant.cne &&
                    note.etudiant.cne.toLowerCase().includes(query))
        );
    }

    return filtered;
});

// Methods
function formatGrade(grade) {
    return grade !== null && grade !== undefined ? grade.toFixed(2) : "-";
}

function getGradeClass(grade) {
    if (grade === null || grade === undefined) return "";
    if (grade >= 16) return "grade-excellent";
    if (grade >= 14) return "grade-very-good";
    if (grade >= 12) return "grade-satisfactory"; // Changed from grade-good
    return "grade-poor"; // Applies to all grades < 12 now
}


function calculateFinalGrade(note) {
    // Use the pourcentages to calculate the weighted final grade
    const rapportWeight = props.pourcentages.pourcentageRapport / 100;
    const soutenanceWeight = props.pourcentages.pourcentageSoutenance / 100;
    const encadrantWeight = props.pourcentages.pourcentageEncadrant / 100;

    // Handle null values by defaulting to 0
    const rapportNote = note.noteRapport || 0;
    const soutenanceNote = note.noteSoutenance || 0;
    const encadrantNote = note.noteEncadrant || 0;

    return (
        rapportNote * rapportWeight +
        soutenanceNote * soutenanceWeight +
        encadrantNote * encadrantWeight
    );
}

function getMention(grade) {
    if (grade >= 16) return "Excellent";
    if (grade >= 14) return "Très Bien";
    if (grade >= 12) return "Passable"; // Changed from "Bien"
    return "Insuffisant"; // Applies to all grades < 12 now
}


function getMentionSeverity(mention) {
    switch (mention) {
        case "Excellent":
            return "success";
        case "Très Bien":
            return "info";
        case "Passable": // This was previously "Bien", but we've changed the mention name
            return "warning";
        case "Insuffisant":
            return "danger";
        default:
            return "secondary";
    }
}

// Check if all required grades are present to show final grade
function canShowFinalGrade(note) {
    return (
        note.noteRapport != null &&
        note.noteSoutenance != null &&
        note.noteEncadrant != null
    );
}

// Check if any notes are available to show details
function hasAnyNotes(note) {
    return (
        note.noteRapport != null ||
        note.noteSoutenance != null ||
        note.noteEncadrant != null
    );
}

// Show details dialog for a student
function showDetails(note) {
    selectedStudent.value = {
        id: note.id,
        nom: note.etudiant.nom,
        prenom: note.etudiant.prenom,
    };
    detailsVisible.value = true;
}
</script>

<style scoped>
.table-card {
    margin-bottom: 2rem;
}

/* Grade color classes */
:deep(.incomplete-grade) {
    font-style: italic;
    color: #888;
}

:deep(.grade-excellent) {
    color: #4caf50;
}

:deep(.grade-very-good) {
    color: #2196f3;
}

:deep(.grade-good) {
    color: #00bcd4;
}

:deep(.grade-satisfactory) {
    color: #ff9800;
}

:deep(.grade-poor) {
    color: #f44336;
}
</style>