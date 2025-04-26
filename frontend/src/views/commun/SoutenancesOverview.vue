<template>
    <div class="soutenances-overview">
        <Toast />

        <!-- Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher une soutenance..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="page-header">
            <h1 class="page-title">Mes Soutenances</h1>
            <div class="date-selector">
                <DatePicker
                    v-model="selectedDate"
                    placeholder="Filtrer par date"
                    @date-select="filterSoutenancesByDate"
                    showIcon
                    dateFormat="dd/mm/yy"
                />
                <Button
                    label="Aujourd'hui"
                    icon="pi pi-calendar"
                    class="p-button-outlined p-button-primary"
                    @click="showTodaySoutenances"
                />
                <Button
                    label="Toutes"
                    icon="pi pi-calendar-times"
                    class="p-button-outlined"
                    @click="resetDateFilter"
                />
            </div>
        </div>

        <!-- Today's soutenances section -->
        <Card class="soutenances-card today-soutenances">
            <template #title>
                <div class="card-title">
                    <i
                        class="pi pi-calendar"
                        style="font-size: 1.5rem; margin-right: 0.5rem"
                    ></i>
                    Soutenances d'aujourd'hui
                    <span class="date-badge">{{ formattedToday }}</span>
                </div>
            </template>
            <template #content>
                <div v-if="loading" class="loading-container">
                    <ProgressSpinner />
                    <p>Chargement des soutenances...</p>
                </div>

                <div
                    v-else-if="todaySoutenances.length === 0"
                    class="empty-state"
                >
                    <i
                        class="pi pi-info-circle"
                        style="font-size: 2rem; color: var(--primary-color)"
                    ></i>
                    <p>Aucune soutenance programmée pour aujourd'hui</p>
                </div>

                <DataTable
                    v-else
                    :value="todaySoutenances"
                    stripedRows
                    class="p-datatable-sm"
                    :loading="loading"
                    :paginator="todaySoutenances.length > 5"
                    :rows="5"
                    dataKey="id"
                    emptyMessage="Aucune soutenance trouvée pour aujourd'hui"
                >
                    <Column
                        field="heure"
                        header="Heure"
                        style="width: 8rem"
                    ></Column>
                    <Column
                        field="salle.nom"
                        header="Salle"
                        style="width: 8rem"
                    ></Column>
                    <Column header="Binôme">
                        <template #body="slotProps">
                            <div class="binome-students">
                                <div class="student">
                                    <i class="pi pi-user"></i>
                                    <span
                                        >{{
                                            slotProps.data.binome?.etudiant1
                                                ?.prenom
                                        }}
                                        {{
                                            slotProps.data.binome?.etudiant1
                                                ?.nom
                                        }}</span
                                    >
                                </div>
                                <div
                                    v-if="slotProps.data.binome?.etudiant2"
                                    class="student"
                                >
                                    <i class="pi pi-user"></i>
                                    <span
                                        >{{
                                            slotProps.data.binome?.etudiant2
                                                ?.prenom
                                        }}
                                        {{
                                            slotProps.data.binome?.etudiant2
                                                ?.nom
                                        }}</span
                                    >
                                </div>
                            </div>
                        </template>
                    </Column>
                    <Column field="binome.sujet.titre" header="Sujet"></Column>
                    <Column header="Actions">
                        <template #body="slotProps">
                            <div class="action-buttons">
                                <Button
                                    icon="pi pi-file-pdf"
                                    class="p-button-rounded p-button-primary p-button-sm"
                                    tooltip="Évaluer le rapport"
                                    @click="navigateToReportGrading(slotProps.data)"
                                />
                                <Button
                                    icon="pi pi-microphone"
                                    class="p-button-rounded p-button-success p-button-sm"
                                    tooltip="Évaluer la soutenance"
                                    :disabled="!isSoutenanceDay(slotProps.data)"
                                    @click="navigateToSoutenanceGrading(slotProps.data)"
                                />
                            </div>
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>

        <!-- All/Filtered soutenances section -->
        <Card class="soutenances-card all-soutenances">
            <template #title>
                <div class="card-title">
                    <i
                        class="pi pi-calendar-plus"
                        style="font-size: 1.5rem; margin-right: 0.5rem"
                    ></i>
                    {{
                        selectedDate
                            ? "Soutenances du " + formatDate(selectedDate)
                            : "Toutes les soutenances"
                    }}
                </div>
            </template>
            <template #content>
                <div v-if="loading" class="loading-container">
                    <ProgressSpinner />
                    <p>Chargement des soutenances...</p>
                </div>

                <div
                    v-else-if="filteredSoutenances.length === 0"
                    class="empty-state"
                >
                    <i
                        class="pi pi-info-circle"
                        style="font-size: 2rem; color: var(--primary-color)"
                    ></i>
                    <p>Aucune soutenance trouvée</p>
                </div>

                <DataTable
                    v-else
                    :value="filteredSoutenances"
                    stripedRows
                    class="p-datatable-sm"
                    :loading="loading"
                    :paginator="filteredSoutenances.length > 10"
                    :rows="10"
                    dataKey="id"
                    emptyMessage="Aucune soutenance trouvée"
                >
                    <Column field="date" header="Date" style="width: 8rem">
                        <template #body="slotProps">
                            {{ formatDate(slotProps.data.date) }}
                        </template>
                    </Column>
                    <Column
                        field="heure"
                        header="Heure"
                        style="width: 8rem"
                    ></Column>
                    <Column
                        field="salle.nom"
                        header="Salle"
                        style="width: 8rem"
                    ></Column>
                    <Column header="Binôme">
                        <template #body="slotProps">
                            <div class="binome-students">
                                <div class="student">
                                    <i class="pi pi-user"></i>
                                    <span
                                        >{{
                                            slotProps.data.binome?.etudiant1
                                                ?.prenom
                                        }}
                                        {{
                                            slotProps.data.binome?.etudiant1
                                                ?.nom
                                        }}</span
                                    >
                                </div>
                                <div
                                    v-if="slotProps.data.binome?.etudiant2"
                                    class="student"
                                >
                                    <i class="pi pi-user"></i>
                                    <span
                                        >{{
                                            slotProps.data.binome?.etudiant2
                                                ?.prenom
                                        }}
                                        {{
                                            slotProps.data.binome?.etudiant2
                                                ?.nom
                                        }}</span
                                    >
                                </div>
                            </div>
                        </template>
                    </Column>
                    <Column field="binome.sujet.titre" header="Sujet"></Column>
                    <Column header="Actions">
                        <template #body="slotProps">
                            <div class="action-buttons">
                                <Button
                                    icon="pi pi-file-pdf"
                                    class="p-button-rounded p-button-primary p-button-sm"
                                    tooltip="Évaluer le rapport"
                                    @click="navigateToReportGrading(slotProps.data)"
                                />
                                <Button
                                    icon="pi pi-microphone"
                                    class="p-button-rounded p-button-success p-button-sm"
                                    tooltip="Évaluer la soutenance"
                                    :disabled="!isSoutenanceDay(slotProps.data)"
                                    @click="navigateToSoutenanceGrading(slotProps.data)"
                                />
                            </div>
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import ApiService from "@/services/ApiService";
import AuthService from "@/services/AuthService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";

// Import PrimeVue components
import Card from "primevue/card";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Button from "primevue/button";
import DatePicker from "primevue/calendar";
import Toast from "primevue/toast";
import ProgressSpinner from "primevue/progressspinner";

// Component state
const soutenances = ref([]);
const selectedDate = ref(null);
const loading = ref(false);
const searchQuery = ref("");

// Services
const toast = useToast();
const router = useRouter();

// Get current user
const currentUser = AuthService.getCurrentUser();

// Today's date in ISO format for comparison
const today = new Date();
today.setHours(0, 0, 0, 0);
const todayISODate = today.toISOString().split("T")[0];

// Formatted today's date for display
const formattedToday = computed(() => {
    return formatDate(today);
});

// Computed property for today's soutenances
const todaySoutenances = computed(() => {
    return soutenances.value.filter((soutenance) => {
        return soutenance.date === todayISODate;
    });
});

// Filtered soutenances
const filteredSoutenances = computed(() => {
    let filtered = [...soutenances.value];

    // Apply date filter
    if (selectedDate.value) {
        const selectedDateStr = formatDateToISOString(selectedDate.value);
        filtered = filtered.filter(
            (soutenance) => soutenance.date === selectedDateStr
        );
    }

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter((soutenance) => {
            const etudiant1Name =
                `${soutenance.binome?.etudiant1?.prenom} ${soutenance.binome?.etudiant1?.nom}`.toLowerCase();
            const etudiant2Name = soutenance.binome?.etudiant2
                ? `${soutenance.binome.etudiant2.prenom} ${soutenance.binome.etudiant2.nom}`.toLowerCase()
                : "";
            const sujetTitre =
                soutenance.binome?.sujet?.titre?.toLowerCase() || "";
            const salleNom = soutenance.salle?.nom.toLowerCase() || "";

            return (
                etudiant1Name.includes(query) ||
                etudiant2Name.includes(query) ||
                sujetTitre.includes(query) ||
                salleNom.includes(query)
            );
        });
    }

    return filtered;
});

// Fetch data on component mount
onMounted(async () => {
    // Check if user is jury, redirect if not
    if (currentUser?.role !== "JURY") {
        toast.add({
            severity: "error",
            summary: "Accès refusé",
            detail: "Cette page est réservée aux membres du jury",
            life: 5000,
        });
        router.push("/");
        return;
    }
    
    await fetchSoutenances();
});

// Format date for display
function formatDate(dateStr) {
    if (!dateStr) return "";

    const date = new Date(dateStr);
    // Use consistent format: DD/MM/YYYY
    return date.toLocaleDateString("fr-FR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

// Format date to ISO string for API comparison
function formatDateToISOString(date) {
    if (!date) return null;

    // Create a new date object to avoid modifying the original
    const newDate = new Date(date);
    // Ensure the date is interpreted correctly - add timezone offset to get consistent dates
    newDate.setHours(12, 0, 0, 0); // Set to noon to avoid timezone issues
    // Return ISO date string (YYYY-MM-DD)
    return newDate.toISOString().split("T")[0];
}

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

// Methods for fetching data
async function fetchSoutenances() {
    loading.value = true;
    try {
        const response = await ApiService.get("/grading/jury/soutenances");
        soutenances.value = response;
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des soutenances");
    } finally {
        loading.value = false;
    }
}

// Filter soutenances by date - computed property handles this
function filterSoutenancesByDate() {
    // No need for implementation as computed property handles it
}

// Show today's soutenances
function showTodaySoutenances() {
    selectedDate.value = new Date();
}

// Reset date filter
function resetDateFilter() {
    selectedDate.value = null;
}

// Check if soutenance can be graded (only on the day of the defense)
function isSoutenanceDay(soutenance) {
    return soutenance.date === todayISODate;
}

// Navigation functions
function navigateToReportGrading(soutenance) {
    // Store soutenance ID in session storage to pass to the grading page
    sessionStorage.setItem("selectedSoutenanceId", soutenance.id);
    router.push("/jury/report-evaluation");
}

function navigateToSoutenanceGrading(soutenance) {
    // Only allow navigation if it's the day of the soutenance
    if (!isSoutenanceDay(soutenance)) {
        toast.add({
            severity: "warn",
            summary: "Action non disponible",
            detail: "L'évaluation de la soutenance n'est disponible que le jour même",
            life: 3000,
        });
        return;
    }

    // Store soutenance ID in session storage to pass to the grading page
    sessionStorage.setItem("selectedSoutenanceId", soutenance.id);
    router.push("/grading/jury/grading");
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
.soutenances-overview {
    width: 100%;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
}

.page-title {
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    margin: 0;
}

.date-selector {
    display: flex;
    gap: 0.5rem;
}

.soutenances-card {
    margin-bottom: 2rem;
}

.card-title {
    display: flex;
    align-items: center;
    font-size: 1.2rem;
    font-weight: 600;
}

.date-badge {
    margin-left: 1rem;
    background-color: var(--primary-color);
    color: white;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.9rem;
}

.binome-students {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.student {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    gap: 1rem;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    text-align: center;
    color: var(--text-color-secondary);
}

/* Responsive styles */
@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        align-items: flex-start;
    }

    .date-selector {
        width: 100%;
        flex-wrap: wrap;
    }
}
</style>