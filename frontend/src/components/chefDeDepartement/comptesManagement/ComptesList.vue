<template>
    <Card class="table-card">
        <template #content>
            <DataTable
                :value="filteredComptes"
                :loading="loading"
                responsiveLayout="scroll"
                stripedRows
                :paginator="filteredComptes.length > 10"
                :rows="10"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 20, 50]"
                currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} comptes"
                emptyMessage="Aucun compte trouvé"
            >
                <Column
                    field="nom"
                    header="Nom"
                    sortable
                    style="min-width: 10rem"
                ></Column>
                <Column
                    field="prenom"
                    header="Prénom"
                    sortable
                    style="min-width: 10rem"
                ></Column>
                <Column
                    field="email"
                    header="Email"
                    sortable
                    style="min-width: 15rem"
                ></Column>
                <Column
                    v-if="selectedRole === 'ETUDIANT'"
                    field="cne"
                    header="CNE"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.cne || "-" }}
                    </template>
                </Column>
                <Column
                    v-if="selectedRole !== 'ETUDIANT'"
                    field="cni"
                    header="CNI"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.cni || "-" }}
                    </template>
                </Column>
                <Column
                    v-if="selectedRole === 'ETUDIANT'"
                    field="filiereName"
                    header="Filière"
                    sortable
                    style="min-width: 8rem"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.filiereName || "-" }}
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
                                icon="pi pi-pencil"
                                class="p-button-rounded p-button-outlined p-button-info mr-2"
                                @click="onEdit(slotProps.data)"
                                tooltip="Modifier"
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
    comptes: {
        type: Array,
        default: () => [],
    },
    loading: {
        type: Boolean,
        default: false,
    },
    selectedRole: {
        type: String,
        required: true,
    },
    searchQuery: {
        type: String,
        default: "",
    },
});

const emit = defineEmits(["edit", "delete"]);

// Computed filtered comptes with optimized filtering
const filteredComptes = computed(() => {
    const query = props.searchQuery.toLowerCase().trim();
    if (!query) return props.comptes;

    return props.comptes.filter((compte) => {
        const searchableFields = [
            compte.nom,
            compte.prenom,
            compte.email,
            compte.cne,
            compte.cni,
            compte.filiereName,
        ]
            .filter(Boolean)
            .map((field) => field.toLowerCase());

        return searchableFields.some((field) => field.includes(query));
    });
});

// Event handlers
function onEdit(compte) {
    emit("edit", compte);
}

function onDelete(compte) {
    emit("delete", compte);
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
