<template>
    <Card class="table-card">
        <template #title>
            <div class="card-header">
                <h3>Demandes de Binôme en Attente</h3>
                <Badge
                    :value="pendingRequests.length.toString()"
                    severity="info"
                />
            </div>
        </template>
        <template #content>
            <div v-if="loading" class="loading-container">
                <ProgressSpinner />
                <p>Chargement des demandes...</p>
            </div>
            <div
                v-else-if="pendingRequests.length === 0"
                class="empty-state"
            >
                <i class="pi pi-users" style="font-size: 3rem"></i>
                <h3>Aucune demande en attente</h3>
                <p>Vous n'avez pas de demandes de binôme en attente.</p>
                <Button
                    label="Choisir un Binôme"
                    icon="pi pi-users"
                    @click="$emit('go-to-choose')"
                />
            </div>
            <DataTable
                v-else
                :value="pendingRequests"
                :loading="loading"
                stripedRows
                responsiveLayout="scroll"
            >
                <Column field="demandeurNom" header="Nom" style="min-width: 10rem">
                    <template #body="slotProps">
                        {{ slotProps.data.demandeurNom }} {{ slotProps.data.demandeurPrenom }}
                    </template>
                </Column>
                <Column field="dateDemande" header="Date de demande" style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ formatDate(slotProps.data.dateDemande) }}
                    </template>
                </Column>
                <Column header="Actions" style="min-width: 10rem; text-align: center">
                    <template #body="slotProps">
                        <div class="action-buttons">
                            <Button
                                icon="pi pi-check"
                                class="p-button-rounded p-button-outlined p-button-success mr-2"
                                @click="$emit('accept', slotProps.data.id)"
                                :loading="processingRequestId === slotProps.data.id && processingAction === 'accept'"
                                :disabled="isProcessingAnyRequest"
                                tooltip="Accepter"
                                tooltipOptions="top"
                            />
                            <Button
                                icon="pi pi-times"
                                class="p-button-rounded p-button-outlined p-button-danger"
                                @click="$emit('reject', slotProps.data.id)"
                                :loading="processingRequestId === slotProps.data.id && processingAction === 'reject'"
                                :disabled="isProcessingAnyRequest"
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
import { computed } from 'vue';
import Card from 'primevue/card';
import Badge from 'primevue/badge';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';

const props = defineProps({
    pendingRequests: {
        type: Array,
        default: () => []
    },
    loading: {
        type: Boolean,
        default: false
    },
    processingRequestId: {
        type: Number,
        default: null
    },
    processingAction: {
        type: String,
        default: null
    }
});

const emit = defineEmits(['accept', 'reject', 'go-to-choose']);

const isProcessingAnyRequest = computed(() => props.processingRequestId !== null);

// Format date function
const formatDate = (dateString) => {
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
</script>

<style scoped>
.table-card {
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

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
}

.loading-container p {
    margin-top: 16px;
    color: var(--text-color-secondary);
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 40px 20px;
}

.empty-state i {
    color: var(--primary-color);
    opacity: 0.6;
    margin-bottom: 16px;
}

.empty-state h3 {
    margin: 0 0 8px;
    color: var(--text-color);
}

.empty-state p {
    margin: 0 0 24px;
    color: var(--text-color-secondary);
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}

.mr-2 {
    margin-right: 0.5rem;
}
</style>
