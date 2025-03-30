<template>
    <div class="notes-management">
        <Toast />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un binôme..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Notes Header -->
        <NotesHeader
            v-model:selectedFiliere="selectedFiliere"
            :filieres="filieres"
            @filiereChange="handleFiliereChange"
            @print="printTable"
        />

        <!-- Notes List -->
        <NotesList
            :notes="notes"
            :selectedFiliere="selectedFiliere"
            :searchQuery="searchQuery"
            :loading="loading"
            :pourcentages="pourcentages"
        />

        <!-- Print button is kept in the NotesHeader component, but the print feature is removed -->
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useToast } from "primevue/usetoast";
import ApiService from "@/services/ApiService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import NotesHeader from "@/components/chefDeDepartement/notesManagement/NotesHeader.vue";
import NotesList from "@/components/chefDeDepartement/notesManagement/NotesList.vue";

// Import PrimeVue components
import Toast from "primevue/toast";

// Component state
const notes = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const loading = ref(false);
const searchQuery = ref("");

// Services
const toast = useToast();

// State for grade percentages
const pourcentages = ref({
    pourcentageRapport: 40,
    pourcentageSoutenance: 40,
    pourcentageEncadrant: 20,
});

// No longer needed after removing print feature

// Print function (placeholder for future implementation)
function printTable() {
    // Print functionality removed as requested
    console.log("Print button clicked - functionality to be implemented later");
}

// Fetch data on component mount
onMounted(async () => {
    await fetchNotes();
});

// Watch for filieres changes to set default value
watch(
    filieres,
    (newFilieres) => {
        if (newFilieres && newFilieres.length > 0 && !selectedFiliere.value) {
            selectedFiliere.value = newFilieres[0].id;
            console.log(
                "Automatically selected first filière:",
                newFilieres[0].nom
            );
        }
    },
    { immediate: true }
);

// Methods for fetching data
async function fetchNotes() {
    loading.value = true;
    try {
        const response = await ApiService.get("/chef_de_departement/notes");

        // Extract notes from response
        if (response && response.notes) {
            notes.value = response.notes;
            console.log("Notes loaded:", notes.value.length);
        } else {
            notes.value = [];
            console.warn("No notes found in response", response);
        }

        // Extract filières if present
        if (response && response.filieres && response.filieres.length > 0) {
            filieres.value = response.filieres;
            console.log("Filières loaded:", filieres.value.length);
        }

        // Extract percentages if present
        if (response && response.pourcentages) {
            pourcentages.value = response.pourcentages;
            console.log("Grade percentages loaded:", pourcentages.value);
        }
    } catch (error) {
        console.error("Error fetching notes:", error);
        handleApiError(error, "Erreur lors du chargement des notes");
    } finally {
        loading.value = false;
    }
}

function handleFiliereChange() {
    // We don't need to fetch data again as we'll filter client-side
    console.log("Filière selected:", selectedFiliere.value);
}

// Handle search from UserInfoHeader
function handleHeaderSearch(query) {
    searchQuery.value = query;
}

// These methods are now handled in the NotesList component

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

<style>
.notes-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Print styles removed as requested */
</style>
