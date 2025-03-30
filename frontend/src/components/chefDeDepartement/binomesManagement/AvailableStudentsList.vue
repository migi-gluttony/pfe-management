<template>
    <Card class="available-students-card">
        <template #title>
            <div class="card-header">
                <h3>Étudiants Sans Binôme</h3>
                <Badge
                    :value="filteredStudents.length.toString()"
                    severity="info"
                />
            </div>
        </template>
        <template #content>
            <DataTable
                :value="filteredStudents"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredStudents.length > 5"
                :rows="5"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 20]"
                emptyMessage="Aucun étudiant disponible pour cette filière"
            >
                <Column
                    field="nom"
                    header="Nom"
                    sortable
                    style="min-width: 10rem"
                />
                <Column
                    field="prenom"
                    header="Prénom"
                    sortable
                    style="min-width: 10rem"
                />
                <Column
                    field="cne"
                    header="CNE"
                    sortable
                    style="min-width: 8rem"
                />
                <Column
                    header="Actions"
                    style="min-width: 8rem; text-align: center"
                >
                    <template #body="slotProps">
                        <Button
                            icon="pi pi-plus"
                            class="p-button-rounded p-button-outlined p-button-success"
                            @click="onAddToBinome(slotProps.data)"
                            tooltip="Ajouter au binôme"
                            tooltipOptions="top"
                        />
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
import Badge from "primevue/badge";

const props = defineProps({
    availableStudents: {
        type: Array,
        default: () => [],
    },
    selectedFiliere: {
        type: Number,
        required: true,
    },
    filieres: {
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

const emit = defineEmits(["addToBinome"]);

// Get the selected filiere object
const selectedFiliereObj = computed(() => {
    return props.filieres.find((f) => f.id === props.selectedFiliere) || null;
});

// Filter students by filiere and search query
const filteredStudents = computed(() => {
    const query = props.searchQuery.toLowerCase().trim();

    // First filter by filiere
    let students = props.availableStudents.filter((student) => {
        // Try to determine the student's filiere
        if (student.filiereId) {
            return student.filiereId === props.selectedFiliere;
        }

        const filiereName = selectedFiliereObj.value?.nom;
        if (student.filiereName && filiereName) {
            return student.filiereName === filiereName;
        }

        // If we can't determine the student's filiere, include them
        return true;
    });

    // Then filter by search query if provided
    if (query) {
        students = students.filter(
            (student) =>
                student.nom?.toLowerCase().includes(query) ||
                student.prenom?.toLowerCase().includes(query) ||
                student.cne?.toLowerCase().includes(query)
        );
    }

    return students;
});

// Handle adding a student to a binome
function onAddToBinome(student) {
    emit("addToBinome", student);
}
</script>

<style scoped>
.available-students-card {
    margin-bottom: 2rem;
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
</style>
