<template>
    <Card class="table-card">
        <template #content>
            <DataTable
                :value="filteredSujets"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredSujets.length > 10"
                :rows="10"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 20, 50]"
                currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} sujets"
                emptyMessage="Aucun sujet trouvé"
            >
                <Column
                    field="titre"
                    header="Titre"
                    sortable
                    style="min-width: 12rem"
                ></Column>
                <Column
                    field="theme"
                    header="Thème"
                    sortable
                    style="min-width: 8rem"
                ></Column>
                <Column
                    field="description"
                    header="Description"
                    style="min-width: 20rem"
                >
                    <template #body="slotProps">
                        <div class="description-text">
                            {{ slotProps.data.description }}
                        </div>
                    </template>
                </Column>
                <Column
                    header="Actions"
                    :exportable="false"
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        <div class="action-buttons">
                            <Button
                                icon="pi pi-eye"
                                class="p-button-rounded p-button-outlined p-button-secondary mr-2"
                                @click="$emit('view', slotProps.data)"
                                tooltip="Voir détails"
                                tooltipOptions="top"
                            />
                            <Button
                                icon="pi pi-pencil"
                                class="p-button-rounded p-button-outlined p-button-info mr-2"
                                @click="$emit('edit', slotProps.data)"
                                tooltip="Modifier"
                                tooltipOptions="top"
                            />
                            <Button
                                icon="pi pi-trash"
                                class="p-button-rounded p-button-outlined p-button-danger"
                                @click="$emit('delete', slotProps.data)"
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
    sujets: {
        type: Array,
        default: () => [],
    },
    selectedClass: {
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
    classes: {
        type: Array,
        default: () => [],
    },
});

const emit = defineEmits(["view", "edit", "delete"]);

// Computed filtered sujets
const filteredSujets = computed(() => {
    const query = props.searchQuery.toLowerCase();
    let filtered = props.sujets;

    // Filter by selected class
    if (props.selectedClass) {
        const selectedFiliereNom = props.classes.find(
            (c) => c.id === props.selectedClass
        )?.nom;
        filtered = filtered.filter(
            (sujet) => sujet.filiereName === selectedFiliereNom
        );
    }

    // Filter by search query
    if (query) {
        filtered = filtered.filter(
            (sujet) =>
                sujet.titre.toLowerCase().includes(query) ||
                sujet.theme.toLowerCase().includes(query) ||
                sujet.description.toLowerCase().includes(query)
        );
    }

    return filtered;
});
</script>

<style scoped>
F .description-text {
    max-height: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}
</style>
F
