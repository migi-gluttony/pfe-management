<template>
    <div class="suggestions-management">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un binôme..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Suggestion Header Component (readded for filtering) -->
        <SuggestionHeader
            v-model:selectedFiliere="selectedFiliere"
            :filieres="filieres"
            @change:filiere="handleFiliereChange"
        />

        <!-- Empty State when no suggestions are found -->
        <div v-if="!loading && suggestions.length === 0" class="empty-state">
            <i class="pi pi-inbox empty-icon"></i>
            <h3>Aucune suggestion de sujet</h3>
            <p>Il n'y a pas de suggestions de sujets à afficher pour le moment.</p>
        </div>

        <template v-else>
            <!-- Suggestion List Component -->
            <SuggestionList
                :suggestions="filteredSuggestions"
                :selectedFiliere="selectedFiliere"
                :searchQuery="searchQuery"
                :loading="loading"
                :filieres="filieres"
                @view="viewSuggestion"
                @accept="confirmAccept"
                @reject="confirmReject"
            />
        </template>

        <!-- View Details Dialog (kept in main view as requested) -->
        <Dialog
            v-model:visible="showViewModal"
            :header="viewingSuggestion?.titre"
            :modal="true"
            class="suggestion-dialog"
            :style="{ width: '700px' }"
        >
            <div v-if="viewingSuggestion" class="suggestion-details">
                <div class="detail-section">
                    <h3>Informations du sujet</h3>
                    <div class="detail-item">
                        <span class="detail-label">Thème:</span>
                        <span class="detail-value">{{
                            viewingSuggestion.theme
                        }}</span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Status:</span>
                        <span
                            class="detail-value"
                            :class="getStatusClass(viewingSuggestion.status)"
                        >
                            {{ getStatusLabel(viewingSuggestion.status) }}
                        </span>
                    </div>

                    <div class="detail-item description-item">
                        <span class="detail-label">Description:</span>
                        <p class="detail-value description-text">
                            {{ viewingSuggestion.description }}
                        </p>
                    </div>
                </div>

                <div class="detail-section">
                    <h3>Informations du binôme</h3>
                    <div class="detail-item">
                        <span class="detail-label">Étudiant 1:</span>
                        <span class="detail-value">{{
                            viewingSuggestion.binome.etudiant1Name
                        }}</span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Étudiant 2:</span>
                        <span class="detail-value">{{
                            viewingSuggestion.binome.etudiant2Name || "-"
                        }}</span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Encadrant:</span>
                        <span class="detail-value">{{
                            viewingSuggestion.binome.encadrantName || "-"
                        }}</span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Filière:</span>
                        <span class="detail-value">{{
                            viewingSuggestion.binome.filiereName
                        }}</span>
                    </div>
                </div>

                <div
                    v-if="viewingSuggestion.status === 'en_attente'"
                    class="action-section"
                >
                    <Button
                        label="Accepter"
                        icon="pi pi-check"
                        class="p-button-success mr-2"
                        @click="confirmAccept(viewingSuggestion)"
                    />
                    <Button
                        label="Refuser"
                        icon="pi pi-times"
                        class="p-button-danger"
                        @click="confirmReject(viewingSuggestion)"
                    />
                </div>
            </div>

            <template #footer>
                <Button
                    label="Fermer"
                    icon="pi pi-times"
                    class="p-button-text"
                    @click="showViewModal = false"
                />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import SuggestionHeader from "@/components/chefDeDepartement/sujetSuggestion/SuggestionHeader.vue";
import SuggestionList from "@/components/chefDeDepartement/sujetSuggestion/SuggestionList.vue";

// Import PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";
import Dialog from "primevue/dialog";
import Button from "primevue/button";

// Component state
const suggestions = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const viewingSuggestion = ref(null);
const showViewModal = ref(false);
const loading = ref(false);
const searchQuery = ref("");

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed property for filtered suggestions
const filteredSuggestions = computed(() => {
    const query = searchQuery.value.toLowerCase();
    let filtered = [...suggestions.value];

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
    if (selectedFiliere.value) {
        filtered = filtered.filter(
            (suggestion) =>
                suggestion.binome &&
                suggestion.binome.filiereId === selectedFiliere.value
        );
    }

    return filtered;
});

// Fetch data on component mount
onMounted(async () => {
    await Promise.all([fetchSuggestions(), fetchFilieres()]);
});

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

function handleFiliereChange() {
    console.log("Filière selected:", selectedFiliere.value);
}

// Methods for fetching data
async function fetchSuggestions() {
    loading.value = true;
    try {
        const response = await ApiService.get(
            "/chef_de_departement/sujet-suggestions"
        );
        suggestions.value = response;
        
        // Map filière IDs to suggestions if filières are already loaded
        if (filieres.value.length > 0) {
            mapFiliereIdsToSuggestions();
        } else {
            // Extract filières from suggestions as a fallback
            extractFilieresFromSuggestions(response);
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des suggestions");
        suggestions.value = []; // Return empty array on error
    } finally {
        loading.value = false;
    }
}

async function fetchFilieres() {
    try {
        const response = await ApiService.get("/chef_de_departement/filieres");
        if (Array.isArray(response) && response.length > 0) {
            filieres.value = response;
            
            // Set first filière as default
            if (filieres.value.length > 0) {
                selectedFiliere.value = filieres.value[0].id;
            }

            // Map filière IDs to suggestions if suggestions are already loaded
            if (suggestions.value.length > 0) {
                mapFiliereIdsToSuggestions();
            }
        }
    } catch (error) {
        console.error("Error fetching filières:", error);
        // Try to extract filières from suggestions if they're loaded
        if (suggestions.value.length > 0) {
            extractFilieresFromSuggestions(suggestions.value);
        }
    }
}

// Helper function to extract unique filières from suggestions as a fallback
function extractFilieresFromSuggestions(suggestionsData) {
    const uniqueFilieres = new Map();

    suggestionsData.forEach((suggestion) => {
        if (suggestion.binome && suggestion.binome.filiereName) {
            // If we don't have an ID, generate one based on the index
            const id = suggestion.binome.filiereId || uniqueFilieres.size + 1;
            uniqueFilieres.set(suggestion.binome.filiereName, {
                id,
                nom: suggestion.binome.filiereName,
            });
        }
    });

    if (uniqueFilieres.size > 0) {
        filieres.value = Array.from(uniqueFilieres.values());

        // Always set first filière as default
        if (filieres.value.length > 0) {
            selectedFiliere.value = filieres.value[0].id;
        }
    }
}

// Helper function to add filiereId to suggestions based on filiereName
function mapFiliereIdsToSuggestions() {
    if (filieres.value.length === 0 || suggestions.value.length === 0) return;

    // Create a mapping of filière names to IDs
    const filiereNameToId = {};
    filieres.value.forEach((filiere) => {
        filiereNameToId[filiere.nom] = filiere.id;
    });

    // Add filiereId to each suggestion if it has a filiereName but no filiereId
    suggestions.value.forEach((suggestion) => {
        if (
            suggestion.binome &&
            suggestion.binome.filiereName &&
            filiereNameToId[suggestion.binome.filiereName] &&
            !suggestion.binome.filiereId
        ) {
            suggestion.binome.filiereId =
                filiereNameToId[suggestion.binome.filiereName];
        }
    });
}

// Suggestion management methods
function confirmAccept(suggestion) {
    confirm.require({
        message: `Êtes-vous sûr de vouloir accepter la suggestion "${suggestion.titre}"? Le sujet sera attribué au binôme qui l'a proposé.`,
        header: "Confirmation d'acceptation",
        icon: "pi pi-check",
        acceptClass: "p-button-success",
        accept: () => acceptSuggestion(suggestion),
        reject: () => {
            /* do nothing */
        },
    });
}

function confirmReject(suggestion) {
    confirm.require({
        message: `Êtes-vous sûr de vouloir refuser la suggestion "${suggestion.titre}"?`,
        header: "Confirmation de refus",
        icon: "pi pi-times",
        acceptClass: "p-button-danger",
        accept: () => rejectSuggestion(suggestion),
        reject: () => {
            /* do nothing */
        },
    });
}

async function acceptSuggestion(suggestion) {
    try {
        await ApiService.post(
            `/chef_de_departement/sujet-suggestions/${suggestion.id}/accept`
        );

        // Update local state
        const index = suggestions.value.findIndex(
            (s) => s.id === suggestion.id
        );
        if (index !== -1) {
            suggestions.value[index].status = "accepter";
        }

        toast.add({
            severity: "success",
            summary: "Suggestion acceptée",
            detail: "La suggestion a été acceptée et le sujet attribué au binôme",
            life: 3000,
        });

        // Close modal if open
        if (showViewModal.value) {
            showViewModal.value = false;
        }
    } catch (error) {
        handleApiError(error, "Erreur lors de l'acceptation de la suggestion");
    }
}

async function rejectSuggestion(suggestion) {
    try {
        await ApiService.post(
            `/chef_de_departement/sujet-suggestions/${suggestion.id}/reject`
        );

        // Update local state
        const index = suggestions.value.findIndex(
            (s) => s.id === suggestion.id
        );
        if (index !== -1) {
            suggestions.value[index].status = "refuser";
        }

        toast.add({
            severity: "info",
            summary: "Suggestion refusée",
            detail: "La suggestion a été refusée",
            life: 3000,
        });

        // Close modal if open
        if (showViewModal.value) {
            showViewModal.value = false;
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du refus de la suggestion");
    }
}

// UI helper methods
function viewSuggestion(suggestion) {
    viewingSuggestion.value = suggestion;
    showViewModal.value = true;
}

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

// Error handling
function handleApiError(error, defaultMessage) {
    console.error(defaultMessage, error);

    let errorMessage = defaultMessage;
    if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
    }

    toast.add({
        severity: "error",
        summary: "Erreur",
        detail: errorMessage,
        life: 5000,
    });
}
</script>

<style scoped>
.suggestions-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Empty state styling */
.empty-state {
    text-align: center;
    padding: 3rem 1rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin: 2rem 0;
}

.empty-icon {
    font-size: 4rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.empty-state h3 {
    font-size: 1.5rem;
    color: var(--text-color);
    margin: 1rem 0 0.5rem;
}

.empty-state p {
    color: var(--text-color-secondary);
    margin: 0;
}

/* Details dialog styles */
.suggestion-details {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.detail-section {
    border: 1px solid var(--surface-border);
    padding: 1rem;
    border-radius: 8px;
    background-color: var(--surface-ground);
}

.detail-section h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: var(--primary-color);
    font-size: 1.2rem;
}

.detail-item {
    display: flex;
    margin-bottom: 0.75rem;
}

.description-item {
    flex-direction: column;
}

.detail-label {
    font-weight: 600;
    min-width: 100px;
}

.detail-value {
    flex: 1;
}

.description-text {
    margin-top: 0.5rem;
    white-space: pre-wrap;
}

.action-section {
    display: flex;
    justify-content: flex-end;
    margin-top: 1rem;
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

/* Responsive adjustments */
@media screen and (max-width: 768px) {
    .detail-item {
        flex-direction: column;
    }

    .detail-label {
        margin-bottom: 0.25rem;
    }

    .action-section {
        flex-direction: column;
        gap: 0.5rem;
    }

    .action-section .p-button {
        width: 100%;
        margin-right: 0 !important;
    }
}
</style>