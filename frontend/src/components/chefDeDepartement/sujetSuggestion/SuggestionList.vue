<template>
    <Card class="table-card">
        <template #content>
            <DataTable
                :value="filteredSuggestions"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredSuggestions.length > 10"
                :rows="10"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 20, 50]"
                currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} suggestions"
                emptyMessage="Aucune suggestion trouvée"
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
                    field="binome.etudiant1Name"
                    header="Étudiant 1"
                    sortable
                    style="min-width: 12rem"
                ></Column>
                <Column
                    field="binome.etudiant2Name"
                    header="Étudiant 2"
                    sortable
                    style="min-width: 12rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.binome.etudiant2Name || "-" }}
                    </template>
                </Column>
                <Column
                    field="status"
                    header="Status"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        <span :class="getStatusClass(slotProps.data.status)">{{
                            getStatusLabel(slotProps.data.status)
                        }}</span>
                    </template>
                </Column>
                <Column
                    header="Actions"
                    :exportable="false"
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        <div class="action-buttons">
                            <Button
                                icon="pi pi-eye"
                                class="p-button-rounded p-button-outlined p-button-info mr-2"
                                @click="$emit('view', slotProps.data)"
                                tooltip="Voir détails"
                                tooltipOptions="top"
                            />
                            <Button
                                v-if="slotProps.data.status === 'en_attente'"
                                icon="pi pi-check"
                                class="p-button-rounded p-button-outlined p-button-success mr-2"
                                @click="$emit('accept', slotProps.data)"
                                tooltip="Accepter"
                                tooltipOptions="top"
                            />
                            <Button
                                v-if="slotProps.data.status === 'en_attente'"
                                icon="pi pi-times"
                                class="p-button-rounded p-button-outlined p-button-danger"
                                @click="$emit('reject', slotProps.data)"
                                tooltip="Refuser"
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
    suggestions: {
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

const emit = defineEmits(["view", "accept", "reject"]);

// Computed filtered suggestions
const filteredSuggestions = computed(() => {
    const query = props.searchQuery.toLowerCase();
    let filtered = [...props.suggestions];

    // Filter by search query
    if (query) {
        filtered = filtered.filter(
            (suggestion) =>
                suggestion.titre.toLowerCase().includes(query) ||
                suggestion.theme.toLowerCase().includes(query) ||
                suggestion.description.toLowerCase().includes(query) ||
                (suggestion.binome.etudiant1Name &&
                    suggestion.binome.etudiant1Name
                        .toLowerCase()
                        .includes(query)) ||
                (suggestion.binome.etudiant2Name &&
                    suggestion.binome.etudiant2Name
                        .toLowerCase()
                        .includes(query))
        );
    }

    // Filter by filière if selected
    if (props.selectedFiliere && filtered.length > 0) {
        // Check if suggestions have filiereId property in binome
        const hasFiliereId = filtered.some(
            (suggestion) =>
                suggestion.binome && suggestion.binome.filiereId !== undefined
        );

        if (hasFiliereId) {
            filtered = filtered.filter(
                (suggestion) =>
                    suggestion.binome &&
                    suggestion.binome.filiereId === props.selectedFiliere
            );
        } else {
            // Fallback: try to filter by filière name instead
            const selectedFiliereName = props.filieres.find(
                (f) => f.id === props.selectedFiliere
            )?.nom;
            if (selectedFiliereName) {
                filtered = filtered.filter(
                    (suggestion) =>
                        suggestion.binome &&
                        suggestion.binome.filiereName === selectedFiliereName
                );
            }
        }
    }

    return filtered;
});

// Status helper functions
function getStatusLabel(status) {
    switch (status) {
        case "en_attente":
            return "En attente";
        case "accepter":
            return "Accepté";
        case "refuser":
            return "Refusé";
        default:
            return status;
    }
}

function getStatusClass(status) {
    switch (status) {
        case "en_attente":
            return "status-waiting";
        case "accepter":
            return "status-accepted";
        case "refuser":
            return "status-rejected";
        default:
            return "";
    }
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

/* Status styles */
.status-waiting {
    color: #ff9900;
    font-weight: 600;
}

.status-accepted {
    color: #22c55e;
    font-weight: 600;
}

.status-rejected {
    color: #f43f5e;
    font-weight: 600;
}
</style>
