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
                <Column field="etudiant1.nom" header="Étudiant 1" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ getStudentFullName(slotProps.data.etudiant1) }}
                    </template>
                </Column>
                <Column field="etudiant2.nom" header="Étudiant 2" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ slotProps.data.etudiant2 ? getStudentFullName(slotProps.data.etudiant2) : '-' }}
                    </template>
                </Column>
                <Column field="encadrant.nom" header="Encadrant" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ getEncadrantFullName(slotProps.data.encadrant) }}
                    </template>
                </Column>
                <Column field="sujet.titre" header="Sujet" sortable style="min-width: 15rem"></Column>
                <Column header="Actions" :exportable="false" style="min-width: 10rem">
                    <template #body="slotProps">
                        <div class="action-buttons">
                            <Button
                                icon="pi pi-pencil"
                                class="p-button-rounded p-button-outlined p-button-info mr-2"
                                @click="onEdit(slotProps.data)"
                                tooltip="Changer l'encadrant"
                                tooltipOptions="top"
                            />
                            <Button
                                icon="pi pi-trash"
                                class="p-button-rounded p-button-outlined p-button-danger"
                                @click="onDelete(slotProps.data)"
                                tooltip="Supprimer"
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
    loading: {
        type: Boolean,
        default: false,
    },
    searchQuery: {
        type: String,
        default: "",
    },
});

const emit = defineEmits(["edit", "delete"]);

// Computed filtered binomes
const filteredBinomes = computed(() => {
    const query = props.searchQuery.toLowerCase().trim();
    if (!query) return props.binomes;

    return props.binomes.filter((binome) => 
        (binome.etudiant1?.nom?.toLowerCase().includes(query) || 
         binome.etudiant1?.prenom?.toLowerCase().includes(query)) ||
        (binome.etudiant2?.nom?.toLowerCase().includes(query) || 
         binome.etudiant2?.prenom?.toLowerCase().includes(query)) ||
        (binome.encadrant?.nom?.toLowerCase().includes(query) || 
         binome.encadrant?.prenom?.toLowerCase().includes(query)) ||
        (binome.sujet?.titre?.toLowerCase().includes(query))
    );
});

// Helper methods
function getStudentFullName(student) {
    if (!student) return '-';
    return `${student.nom} ${student.prenom}`;
}

function getEncadrantFullName(encadrant) {
    if (!encadrant) return '-';
    return `${encadrant.nom} ${encadrant.prenom}`;
}

// Event handlers
function onEdit(binome) {
    emit("edit", binome);
}

function onDelete(binome) {
    emit("delete", binome);
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