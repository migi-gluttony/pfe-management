<template>
    <Card class="table-card">
        <template #content>
            <DataTable
                :value="filteredBinomes"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredBinomes.length > 10"
                :rows="10"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 20, 50]"
                currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} binômes"
                emptyMessage="Aucun binôme trouvé"
            >
                <Column
                    field="binome.etudiant1"
                    header="Étudiant 1"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        {{ getStudentName(slotProps.data.binome?.etudiant1) }}
                    </template>
                </Column>
                <Column
                    field="binome.etudiant2"
                    header="Étudiant 2"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        {{
                            slotProps.data.binome?.etudiant2
                                ? getStudentName(
                                      slotProps.data.binome.etudiant2
                                  )
                                : "-"
                        }}
                    </template>
                </Column>
                <Column
                    field="binome.encadrant"
                    header="Encadrant"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        {{ getEncadrantName(slotProps.data.binome?.encadrant) }}
                    </template>
                </Column>
                <Column
                    field="binome.sujet.titre"
                    header="Sujet"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.binome?.sujet?.titre || "-" }}
                    </template>
                </Column>
                <Column
                    field="date"
                    header="Date & Heure"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        <template v-if="slotProps.data.id">
                            {{ formatDate(slotProps.data.date) }} à
                            {{ slotProps.data.heure }}
                        </template>
                        <span v-else>-</span>
                    </template>
                </Column>
                <Column
                    field="salle"
                    header="Salle & Jury"
                    sortable
                    style="min-width: 15rem"
                >
                    <template #body="slotProps">
                        <template v-if="slotProps.data.id">
                            Salle: {{ slotProps.data.salle?.nom }} <br />
                            Jury: {{ getJuryName(slotProps.data.jury1) }},
                            {{ getJuryName(slotProps.data.jury2) }}
                        </template>
                        <span v-else>-</span>
                    </template>
                </Column>
                <Column
                    header="Actions"
                    :exportable="false"
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <div class="action-buttons">
                            <Button
                                v-if="slotProps.data.id"
                                icon="pi pi-pencil"
                                class="p-button-rounded p-button-outlined p-button-info mr-2"
                                @click="$emit('edit', slotProps.data.id)"
                                :disabled="loading"
                                tooltip="Modifier"
                                tooltipOptions="top"
                            />
                            <Button
                                v-else
                                icon="pi pi-calendar-plus"
                                class="p-button-rounded p-button-outlined p-button-success mr-2"
                                @click="$emit('schedule', slotProps.data)"
                                tooltip="Programmer"
                                tooltipOptions="top"
                            />
                            <Button
                                v-if="slotProps.data.id"
                                icon="pi pi-trash"
                                class="p-button-rounded p-button-outlined p-button-danger"
                                @click="$emit('cancel', slotProps.data)"
                                tooltip="Annuler"
                                tooltipOptions="top"
                            />
                        </div>
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script setup>
import { computed } from "vue";
import Card from "primevue/card";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Button from "primevue/button";

const props = defineProps({
    binomes: {
        type: Array,
        default: () => [],
    },
    soutenances: {
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
    filieres: {
        type: Array,
        default: () => [],
    },
});

const emit = defineEmits(["edit", "schedule", "cancel"]);

// Computed filtered binomes
const filteredBinomes = computed(() => {
    if (!props.soutenances.length && !props.binomes.length) return [];

    // Create a merged array of soutenances and binomes without soutenances
    const mergedData = [...props.soutenances];

    // Add binomes that don't have soutenances yet
    props.binomes.forEach((binome) => {
        const hasSoutenance = props.soutenances.some(
            (s) => s.binome?.id === binome.id
        );
        if (!hasSoutenance) {
            // Create an empty soutenance object with binome info
            mergedData.push({ binome });
        }
    });

    let filtered = mergedData;

    // Filter by selected filiere
    if (props.selectedFiliere) {
        filtered = filtered.filter((item) => {
            return (
                item.binome?.filiereName &&
                props.filieres.some(
                    (f) =>
                        f.id === props.selectedFiliere &&
                        f.nom === item.binome.filiereName
                )
            );
        });
    }

    // Filter by search query
    if (props.searchQuery) {
        const query = props.searchQuery.toLowerCase();
        filtered = filtered.filter((item) => {
            const binome = item.binome;
            if (!binome) return false;

            return (
                binome.etudiant1?.nom?.toLowerCase().includes(query) ||
                binome.etudiant1?.prenom?.toLowerCase().includes(query) ||
                (binome.etudiant2 &&
                    (binome.etudiant2.nom?.toLowerCase().includes(query) ||
                        binome.etudiant2.prenom
                            ?.toLowerCase()
                            .includes(query))) ||
                binome.encadrant?.nom?.toLowerCase().includes(query) ||
                binome.encadrant?.prenom?.toLowerCase().includes(query) ||
                binome.sujet?.titre?.toLowerCase().includes(query)
            );
        });
    }

    return filtered;
});

// Helper methods
function getStudentName(student) {
    if (!student) return "N/A";
    return `${student.prenom} ${student.nom}`;
}

function getEncadrantName(encadrant) {
    if (!encadrant) return "N/A";
    return `${encadrant.prenom} ${encadrant.nom}`;
}

function getJuryName(jury) {
    if (!jury) return "N/A";
    return `${jury.prenom} ${jury.nom}`;
}

function formatDate(date) {
    if (!date) return "N/A";

    // If it's already a string in a date format, return it formatted
    if (typeof date === "string") {
        return new Date(date).toLocaleDateString("fr-FR");
    }

    // If it's a Date object
    if (date instanceof Date) {
        return date.toLocaleDateString("fr-FR");
    }

    return "N/A";
}
</script>

<style scoped>
.table-card {
    margin-bottom: 2rem;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}
</style>
