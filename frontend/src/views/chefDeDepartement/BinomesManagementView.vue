<template>
    <div class="binome-management">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header with Search -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un binôme..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Binomes Header -->
        <BinomesHeader
            :selectedFiliere="selectedFiliere"
            :filieres="filieres"
            @filiere-change="handleFiliereChange"
            @add="openAddModal"
        />

        <!-- Binomes List -->
        <BinomesList
            :binomes="binomes"
            :loading="loading"
            :searchQuery="searchQuery"
            @edit="openEditModal"
            @delete="confirmDelete"
        />

        <!-- Add Binome Modal -->
        <BinomesAddModal
            v-model:visible="showAddModal"
            :selectedFiliere="selectedFiliere"
            :availableStudents="availableStudents"
            :encadrants="encadrants"
            :availableSujets="availableSujets"
            :submitting="submitting"
            @submit="addBinome"
            @cancel="closeModals"
        />

        <!-- Edit Binome Modal -->
        <BinomesEditModal
            v-model:visible="showEditModal"
            :binome="editingBinome"
            :encadrants="encadrants"
            :submitting="submitting"
            @submit="saveEditedBinome"
            @cancel="closeModals"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// Import components
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import BinomesHeader from "@/components/chefDeDepartement/binomesManagement/BinomesHeader.vue";
import BinomesList from "@/components/chefDeDepartement/binomesManagement/BinomesList.vue";
import BinomesAddModal from "@/components/chefDeDepartement/binomesManagement/BinomesAddModal.vue";
import BinomesEditModal from "@/components/chefDeDepartement/binomesManagement/BinomesEditModal.vue";

// Import PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Component state
const binomes = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const availableStudents = ref([]);
const encadrants = ref([]);
const availableSujets = ref([]);
const editingBinome = ref(null);
const searchQuery = ref("");

// UI state
const showAddModal = ref(false);
const showEditModal = ref(false);
const loading = ref(false);
const submitting = ref(false);

// Services
const toast = useToast();
const confirm = useConfirm();

// Fetch data on component mount
onMounted(async () => {
    await fetchData();
});

// Handler for search from header component
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

// Methods for fetching data
async function fetchData() {
    loading.value = true;
    try {
        const response = await ApiService.get("/chef_de_departement/binomes");

        binomes.value = response.binomes || [];
        filieres.value = response.filieres || [];
        availableStudents.value = response.availableStudents || [];
        encadrants.value = response.encadrants || [];
        availableSujets.value = response.availableSujets || [];

        // Set first filière as default if none selected
        if (filieres.value.length > 0 && !selectedFiliere.value) {
            selectedFiliere.value = filieres.value[0].id;
            // Immediately fetch binomes for the selected filière
            fetchBinomesForFiliere(selectedFiliere.value);
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des données");
    } finally {
        loading.value = false;
    }
}

// Function to get binomes for a specific filière
async function fetchBinomesForFiliere(filiereId) {
    if (!filiereId) return;

    loading.value = true;
    try {
        const response = await ApiService.get(
            `/chef_de_departement/binomes?filiereId=${filiereId}`
        );
        
        // Update binomes list
        binomes.value = response.binomes || [];
        
        // Update available students with filière information
        availableStudents.value = (response.availableStudents || []).map(student => ({
            ...student,
            filiereName: Number(filiereId) // Use Number to ensure consistent type comparison
        }));
        
        // Update available subjects with filière information
        availableSujets.value = (response.availableSujets || []).map(sujet => ({
            ...sujet,
            filiereId: Number(filiereId) // Use Number to ensure consistent type comparison
        }));
        
        console.log(`Fetched ${availableStudents.value.length} students for filière ${filiereId}`);
        console.log(`Fetched ${availableSujets.value.length} subjects for filière ${filiereId}`);
    } catch (error) {
        handleApiError(
            error,
            "Erreur lors du chargement des binômes pour cette filière"
        );
    } finally {
        loading.value = false;
    }
}

async function handleFiliereChange(filiereId) {
    selectedFiliere.value = filiereId;
    await fetchBinomesForFiliere(filiereId);
}

// CRUD operations
async function addBinome(formData) {
    if (!validateBinomeData(formData)) return;

    submitting.value = true;
    try {
        const response = await ApiService.post(
            "/chef_de_departement/binomes",
            formData
        );
        binomes.value.push(response);

        // Close modal
        showAddModal.value = false;

        // Show success message
        toast.add({
            severity: "success",
            summary: "Binôme ajouté",
            detail: "Le binôme a été ajouté avec succès",
            life: 3000,
        });

        // Refresh data to update available students and subjects
        await fetchBinomesForFiliere(selectedFiliere.value);
    } catch (error) {
        handleApiError(error, "Erreur lors de l'ajout du binôme");
    } finally {
        submitting.value = false;
    }
}

async function saveEditedBinome(formData) {
    if (!formData.encadrantId) {
        showValidationError("Veuillez sélectionner un encadrant");
        return;
    }

    submitting.value = true;
    try {
        const response = await ApiService.put(
            `/chef_de_departement/binomes/${editingBinome.value.id}`,
            formData
        );

        // Update local data
        const index = binomes.value.findIndex(
            (b) => b.id === editingBinome.value.id
        );
        if (index !== -1) {
            binomes.value[index] = response;
        }

        showEditModal.value = false;

        toast.add({
            severity: "success",
            summary: "Binôme mis à jour",
            detail: "L'encadrant du binôme a été modifié avec succès",
            life: 3000,
        });
    } catch (error) {
        handleApiError(error, "Erreur lors de la modification de l'encadrant");
    } finally {
        submitting.value = false;
    }
}

function confirmDelete(binome) {
    const studentNames = binome.etudiant2
        ? `${binome.etudiant1.nom} ${binome.etudiant1.prenom} et ${binome.etudiant2.nom} ${binome.etudiant2.prenom}`
        : `${binome.etudiant1.nom} ${binome.etudiant1.prenom}`;

    confirm.require({
        message: `Êtes-vous sûr de vouloir supprimer le binôme de ${studentNames}?`,
        header: "Confirmation de suppression",
        icon: "pi pi-exclamation-triangle",
        acceptClass: "p-button-danger",
        accept: () => deleteBinome(binome),
        reject: () => {
            /* do nothing */
        },
    });
}

async function deleteBinome(binome) {
    try {
        await ApiService.delete(`/chef_de_departement/binomes/${binome.id}`);

        // Update local data
        binomes.value = binomes.value.filter((b) => b.id !== binome.id);

        toast.add({
            severity: "success",
            summary: "Binôme supprimé",
            detail: "Le binôme a été supprimé avec succès",
            life: 3000,
        });

        // Refresh data to update available students
        await fetchBinomesForFiliere(selectedFiliere.value);
    } catch (error) {
        handleApiError(error, "Erreur lors de la suppression du binôme");
    }
}

// UI control methods
function openAddModal() {
    showAddModal.value = true;
}

function openEditModal(binome) {
    editingBinome.value = binome;
    showEditModal.value = true;
}

function closeModals() {
    showAddModal.value = false;
    showEditModal.value = false;
    editingBinome.value = null;
}

// Validation
function validateBinomeData(binome) {
    if (!binome.etudiant1Id) {
        showValidationError("L'étudiant 1 est obligatoire");
        return false;
    }

    if (!binome.encadrantId) {
        showValidationError("L'encadrant est obligatoire");
        return false;
    }

    if (!binome.sujetId) {
        showValidationError("Le sujet est obligatoire");
        return false;
    }

    return true;
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

function showValidationError(message) {
    toast.add({
        severity: "warn",
        summary: "Validation",
        detail: message,
        life: 5000,
    });
}
</script>

<style scoped>
.binome-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}
</style>
