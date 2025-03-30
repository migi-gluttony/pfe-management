<template>
    <div class="soutenance-management">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un binôme..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Soutenance Header Component -->
        <SoutenanceHeader
            v-model:selectedFiliere="selectedFiliere"
            :filieres="filieres"
            @change:filiere="handleFiliereChange"
        />

        <!-- Empty State when no binomes are found -->
        <div
            v-if="
                !loading &&
                ((binomes.length === 0 && soutenances.length === 0) ||
                    (selectedFiliere && filteredBinomesCount === 0))
            "
            class="empty-state"
        >
            <i class="pi pi-inbox empty-icon"></i>
            <h3>Aucun binôme trouvé</h3>
            <p>Il n'y a pas de binômes à afficher pour le moment.</p>
        </div>

        <!-- Soutenance List Component -->
        <SoutenanceList
            v-else
            :binomes="binomes"
            :soutenances="soutenances"
            :selectedFiliere="selectedFiliere"
            :searchQuery="searchQuery"
            :loading="loading"
            :filieres="filieres"
            @edit="handleEditButtonClick"
            @schedule="openScheduleModal"
            @cancel="confirmCancelSoutenance"
        />

        <!-- Soutenance Form Dialog Component -->
        <SoutenanceFormDialog
            v-model:show="showSoutenanceModal"
            :soutenanceData="currentSoutenance"
            :salles="salles"
            :jurys="jurys"
            :submitting="submitting"
            :validationErrors="validationErrors"
            @save="saveSoutenance"
            @validate="validateSoutenanceRequest"
            @close="closeSoutenanceModal"
        />
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import SoutenanceHeader from "@/components/chefDeDepartement/soutenanceManagement/SoutenanceHeader.vue";
import SoutenanceList from "@/components/chefDeDepartement/soutenanceManagement/SoutenanceList.vue";
import SoutenanceFormDialog from "@/components/chefDeDepartement/soutenanceManagement/SoutenanceFormDialog.vue";

// Import PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Component state
const soutenances = ref([]);
const binomes = ref([]);
const filieres = ref([]);
const jurys = ref([]);
const salles = ref([]);
const selectedFiliere = ref(null);
const loading = ref(false);
const submitting = ref(false);
const searchQuery = ref("");

// Current soutenance state
const currentSoutenance = ref(null);
const showSoutenanceModal = ref(false);

// Validation state
const validationErrors = ref({});

// Services
const toast = useToast();
const confirm = useConfirm();

// Computed property to count filtered binomes for empty state check
const filteredBinomesCount = computed(() => {
    let count = 0;

    // Create a merged array of soutenances and binomes without soutenances
    const mergedData = [...soutenances.value];

    // Add binomes that don't have soutenances yet
    binomes.value.forEach((binome) => {
        const hasSoutenance = soutenances.value.some(
            (s) => s.binome?.id === binome.id
        );
        if (!hasSoutenance) {
            mergedData.push({ binome });
        }
    });

    // Apply filters
    if (selectedFiliere.value) {
        count = mergedData.filter((item) => {
            return (
                item.binome?.filiereName &&
                filieres.value.some(
                    (f) =>
                        f.id === selectedFiliere.value &&
                        f.nom === item.binome.filiereName
                )
            );
        }).length;
    } else {
        count = mergedData.length;
    }

    return count;
});

// Fetch data on component mount
onMounted(async () => {
    await Promise.all([
        fetchSoutenances(),
        fetchBinomes(),
        fetchFilieres(),
        fetchJurys(),
        fetchSalles(),
    ]);

    // Set default selected filiere if available
    if (filieres.value.length > 0) {
        selectedFiliere.value = filieres.value[0].id;
    }
});

// Handler methods
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

function handleFiliereChange() {
    console.log("Filière selected:", selectedFiliere.value);
}

// Data loading functions
async function fetchSoutenances() {
    loading.value = true;
    try {
        console.log("Fetching soutenances...");
        const response = await ApiService.get(
            "/chef_de_departement/soutenances"
        );
        console.log("Soutenances raw response:", response);

        // Check response structure and add safeguards
        if (Array.isArray(response)) {
            soutenances.value = response;
        } else if (response && Array.isArray(response.soutenances)) {
            soutenances.value = response.soutenances;
        } else {
            console.warn("Unexpected soutenances response format:", response);
            soutenances.value = response || [];
        }

        console.log("Processed soutenances:", soutenances.value);
    } catch (error) {
        console.error("Error fetching soutenances:", error);
        handleApiError(error, "Erreur lors du chargement des soutenances");
        soutenances.value = [];
    } finally {
        loading.value = false;
    }
}

async function fetchBinomes() {
    try {
        const response = await ApiService.get("/chef_de_departement/binomes");
        binomes.value = response.binomes || [];
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des binômes");
        binomes.value = [];
    }
}

async function fetchFilieres() {
    try {
        const response = await ApiService.get("/chef_de_departement/filieres");
        filieres.value = response || [];

        // If response has a filieres property, use it
        if (response && Array.isArray(response.filieres)) {
            filieres.value = response.filieres;
        }

        // Try alternative endpoint if needed
        if (filieres.value.length === 0) {
            const altResponse = await ApiService.get(
                "/chef_de_departement/sujets"
            );
            if (altResponse && Array.isArray(altResponse.filieres)) {
                filieres.value = altResponse.filieres;
            }
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des filières");
        filieres.value = [];
    }
}

async function fetchJurys() {
    try {
        // Try with query parameter in URL directly, which is more reliable
        const response = await ApiService.get(
            "/chef_de_departement/comptes?role=JURY"
        );

        // If response has a comptes property, use it; otherwise use the response directly
        const juryData = response.comptes || response || [];

        jurys.value = juryData.map((jury) => ({
            ...jury,
            fullName: `${jury.prenom} ${jury.nom}`,
        }));

        console.log("Jurys loaded:", jurys.value);
    } catch (error) {
        console.error("Raw error loading jurys:", error);
        handleApiError(error, "Erreur lors du chargement des jurys");
        jurys.value = [];
    }
}

async function fetchSalles() {
    try {
        console.log("Fetching salles data...");
        // Try using the chef_de_departement prefix path
        const response = await ApiService.get("/chef_de_departement/salles");
        console.log("Salles response:", response);

        // Check different response formats
        if (Array.isArray(response)) {
            salles.value = response;
        } else if (response && Array.isArray(response.salles)) {
            salles.value = response.salles;
        } else {
            console.warn(
                "Unexpected salles response format, trying alternative endpoint"
            );
            // Try the alternative direct endpoint
            const altResponse = await ApiService.get("/salle");
            if (Array.isArray(altResponse)) {
                salles.value = altResponse;
            } else {
                console.error("Failed to parse salles from both endpoints");
                salles.value = [];
            }
        }

        console.log("Processed salles:", salles.value);
    } catch (error) {
        console.error("Error fetching salles:", error);
        // Try fallback endpoint if first one fails
        try {
            console.log("Trying fallback salles endpoint...");
            const fallbackResponse = await ApiService.get("/salle");
            salles.value = Array.isArray(fallbackResponse)
                ? fallbackResponse
                : [];
            console.log("Fallback salles loaded:", salles.value);
        } catch (fallbackError) {
            console.error("Fallback salles fetch also failed:", fallbackError);
            handleApiError(error, "Erreur lors du chargement des salles");
            salles.value = [];
        }
    }
}

// Modal functions
function openScheduleModal(item) {
    currentSoutenance.value = item;
    validationErrors.value = {};
    showSoutenanceModal.value = true;
}

// Edit button handling
async function handleEditButtonClick(soutenanceId) {
    loading.value = true;

    try {
        console.log(`Edit button clicked for soutenance ID: ${soutenanceId}`);

        // Direct fetch of the specific soutenance by ID
        const soutenanceData = await ApiService.get(
            `/chef_de_departement/soutenances/${soutenanceId}`
        );
        console.log("Fetched soutenance data:", soutenanceData);

        if (!soutenanceData || !soutenanceData.id) {
            throw new Error("Invalid soutenance data returned from API");
        }

        // Store the full soutenance data
        currentSoutenance.value = soutenanceData;

        // Clear any validation errors
        validationErrors.value = {};

        // Open the modal
        showSoutenanceModal.value = true;
    } catch (error) {
        console.error("Error initializing edit form:", error);
        toast.add({
            severity: "error",
            summary: "Erreur",
            detail: "Impossible de charger les données de la soutenance",
            life: 5000,
        });
    } finally {
        loading.value = false;
    }
}

function closeSoutenanceModal() {
    showSoutenanceModal.value = false;
    currentSoutenance.value = null;
    validationErrors.value = {};
}

// Validation functions
async function validateSoutenanceRequest(formData) {
    // Reset validation errors
    validationErrors.value = {};

    // Check if all required fields are present before validating
    if (
        !formData.date ||
        !formData.heure ||
        !formData.binomeId ||
        !formData.salleId ||
        !formData.jury1Id ||
        !formData.jury2Id
    ) {
        return;
    }

    try {
        // Create validation request
        const formattedDate = formatDateForApi(formData.date);
        const formattedTime = formatTimeForApi(formData.heure);

        console.log("Validating with data:", {
            date: formattedDate,
            heure: formattedTime,
            binomeId: formData.binomeId,
            salleId: formData.salleId,
            jury1Id: formData.jury1Id,
            jury2Id: formData.jury2Id,
        });

        // Use the correct DTO format for validation requests
        const validationRequest = formData.id
            ? {
                  id: formData.id,
                  date: formattedDate,
                  heure: formattedTime,
                  salleId: formData.salleId,
                  binomeId: formData.binomeId,
                  jury1Id: formData.jury1Id,
                  jury2Id: formData.jury2Id,
              }
            : {
                  date: formattedDate,
                  heure: formattedTime,
                  salleId: formData.salleId,
                  binomeId: formData.binomeId,
                  jury1Id: formData.jury1Id,
                  jury2Id: formData.jury2Id,
              };

        let endpoint = "/chef_de_departement/soutenances/validate";
        if (formData.id) {
            // For updates, use the specific validation endpoint
            endpoint = `/chef_de_departement/soutenances/${formData.id}/validate`;
        }

        const response = await ApiService.post(endpoint, validationRequest);
        console.log("Validation response:", response);

        if (response && !response.valid && response.errors) {
            // Map errors to form fields
            response.errors.forEach((error) => {
                validationErrors.value[error.field] = error.message;
            });
        }
    } catch (error) {
        console.error("Raw validation error:", error);
        handleApiError(error, "Erreur lors de la validation de la soutenance");
    }
}

// Save operations
async function saveSoutenance(formData) {
    submitting.value = true;
    try {
        // Prepare data
        const formattedDate = formatDateForApi(formData.date);
        const formattedTime = formatTimeForApi(formData.heure);

        console.log("Saving with formatted date and time:", {
            formattedDate,
            formattedTime,
        });

        // Check required fields
        if (
            !formattedDate ||
            !formattedTime ||
            !formData.salleId ||
            !formData.binomeId ||
            !formData.jury1Id ||
            !formData.jury2Id
        ) {
            const missingFields = [];
            if (!formattedDate) missingFields.push("date");
            if (!formattedTime) missingFields.push("heure");
            if (!formData.salleId) missingFields.push("salle");
            if (!formData.binomeId) missingFields.push("binôme");
            if (!formData.jury1Id) missingFields.push("jury 1");
            if (!formData.jury2Id) missingFields.push("jury 2");

            throw new Error(`Champs manquants: ${missingFields.join(", ")}`);
        }

        // Create request that matches the SoutenanceAddRequest or SoutenanceUpdateRequest DTO
        const soutenanceRequest = {
            date: formattedDate,
            heure: formattedTime,
            salleId: formData.salleId,
            binomeId: formData.binomeId,
            jury1Id: formData.jury1Id,
            jury2Id: formData.jury2Id,
        };

        if (formData.id) {
            soutenanceRequest.id = formData.id;
        }

        console.log("Sending soutenance request:", soutenanceRequest);

        let response;
        if (formData.id) {
            // Update existing soutenance
            response = await ApiService.put(
                `/chef_de_departement/soutenances/${formData.id}`,
                soutenanceRequest
            );
            console.log("Update response:", response);

            toast.add({
                severity: "success",
                summary: "Soutenance modifiée",
                detail: "La soutenance a été mise à jour avec succès",
                life: 3000,
            });
        } else {
            // Create new soutenance
            response = await ApiService.post(
                "/chef_de_departement/soutenances",
                soutenanceRequest
            );
            console.log("Create response:", response);

            toast.add({
                severity: "success",
                summary: "Soutenance programmée",
                detail: "La soutenance a été programmée avec succès",
                life: 3000,
            });
        }

        // Close the modal
        closeSoutenanceModal();

        // Refresh the data to ensure we have the latest state
        await fetchSoutenances();
    } catch (error) {
        console.error("Error saving soutenance:", error);
        toast.add({
            severity: "error",
            summary: "Erreur",
            detail:
                error.message ||
                "Erreur lors de l'enregistrement de la soutenance",
            life: 5000,
        });
    } finally {
        submitting.value = false;
    }
}

function confirmCancelSoutenance(soutenance) {
    confirm.require({
        message: `Êtes-vous sûr de vouloir annuler la soutenance du binôme ${
            soutenance.binome?.etudiant1?.prenom || ""
        } ${soutenance.binome?.etudiant1?.nom || ""}${
            soutenance.binome?.etudiant2
                ? ` et ${soutenance.binome.etudiant2.prenom || ""} ${
                      soutenance.binome.etudiant2.nom || ""
                  }`
                : ""
        }?`,
        header: "Confirmation d'annulation",
        icon: "pi pi-exclamation-triangle",
        acceptClass: "p-button-danger",
        accept: () => cancelSoutenance(soutenance),
        reject: () => {
            /* do nothing */
        },
    });
}

async function cancelSoutenance(soutenance) {
    if (!soutenance.id) return;

    try {
        await ApiService.delete(
            `/chef_de_departement/soutenances/${soutenance.id}`
        );

        // Update local data
        soutenances.value = soutenances.value.filter(
            (s) => s.id !== soutenance.id
        );

        toast.add({
            severity: "success",
            summary: "Soutenance annulée",
            detail: "La soutenance a été annulée avec succès",
            life: 3000,
        });
    } catch (error) {
        handleApiError(error, "Erreur lors de l'annulation de la soutenance");
    }
}

// Date/time formatting helper functions
function formatTimeForApi(time) {
    if (!time) return "";

    try {
        if (time instanceof Date) {
            // Ensure we have a valid Date object
            if (isNaN(time.getTime())) {
                console.error("Invalid Date object for time:", time);
                return "";
            }

            // Format as HH:MM
            const hours = time.getHours().toString().padStart(2, "0");
            const minutes = time.getMinutes().toString().padStart(2, "0");
            return `${hours}:${minutes}`;
        }

        if (typeof time === "string") {
            // If it's already a string in HH:MM format, return it
            if (/^\d{1,2}:\d{2}$/.test(time)) {
                return time;
            }

            // Try to parse it as a date string
            const dateObj = new Date(time);
            if (!isNaN(dateObj.getTime())) {
                const hours = dateObj.getHours().toString().padStart(2, "0");
                const minutes = dateObj
                    .getMinutes()
                    .toString()
                    .padStart(2, "0");
                return `${hours}:${minutes}`;
            }
        }

        console.error("Unhandled time format:", time, typeof time);
        return "";
    } catch (error) {
        console.error("Error formatting time:", error);
        return "";
    }
}

function formatDateForApi(date) {
    if (!date) return null;

    try {
        // Handle Date objects
        if (date instanceof Date) {
            // Verify it's a valid date
            if (isNaN(date.getTime())) {
                console.error("Invalid Date object:", date);
                return null;
            }

            // Format as YYYY-MM-DD
            const year = date.getFullYear();
            const month = (date.getMonth() + 1).toString().padStart(2, "0");
            const day = date.getDate().toString().padStart(2, "0");
            return `${year}-${month}-${day}`;
        }

        // Handle string dates
        if (typeof date === "string") {
            // Check if it's already in YYYY-MM-DD format
            if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
                return date;
            }

            // Try to convert other formats to YYYY-MM-DD
            const parsedDate = new Date(date);
            if (!isNaN(parsedDate.getTime())) {
                const year = parsedDate.getFullYear();
                const month = (parsedDate.getMonth() + 1)
                    .toString()
                    .padStart(2, "0");
                const day = parsedDate.getDate().toString().padStart(2, "0");
                return `${year}-${month}-${day}`;
            }
        }

        console.error("Unhandled date format:", date, typeof date);
        return null;
    } catch (error) {
        console.error("Error formatting date:", error);
        return null;
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
.soutenance-management {
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
</style>
