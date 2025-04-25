<template>
    <div class="notes-management">
        <Toast />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un étudiant..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Notes Header -->
        <NotesHeader
            v-model:selectedFiliere="selectedFiliere"
            :filieres="filieres"
            @filiereChange="handleFiliereChange"
            @print="openPrintDialog"
        />

        <!-- Notes List -->
        <NotesList
            :notes="notes"
            :selectedFiliere="selectedFiliere"
            :searchQuery="searchQuery"
            :loading="loading"
            :pourcentages="pourcentages"
        />

        <!-- Print Dialog -->
        <PrintDialog
            v-model:visible="showPrintDialog"
            :notes="notes"
            :filieres="filieres"
            :selectedFiliere="selectedFiliere"
            @print="printNotes"
            @print-evaluation-sheet="printEvaluationSheet"
        />

        <!-- Printable Notes Component (hidden in normal view) -->
        <PrintableNotes
            v-if="!isPrintingEvaluationSheet"
            ref="printableNotesRef"
            :notes="notes"
            :filieres="filieres"
            :selectedFiliere="selectedFiliere"
            :showAllFilieres="printSettings.showAllFilieres"
            :showDetails="printSettings.showDetails"
            :pourcentages="pourcentages"
        />
        
        <!-- Student Evaluation Sheet (hidden in normal view) -->
        <StudentEvaluationSheet
            v-if="selectedEvaluationStudent && isPrintingEvaluationSheet"
            ref="evaluationSheetRef"
            :student="selectedEvaluationStudent"
            :note="selectedEvaluationNote"
            :noteDetail="selectedEvaluationDetail"
            :filiereName="selectedEvaluationFiliereName"
            :pourcentages="pourcentages"
        />
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useToast } from "primevue/usetoast";
import ApiService from "@/services/ApiService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import NotesHeader from "@/components/chefDeDepartement/notesManagement/NotesHeader.vue";
import NotesList from "@/components/chefDeDepartement/notesManagement/NotesList.vue";
import PrintDialog from "@/components/chefDeDepartement/notesManagement/PrintDialog.vue";
import PrintableNotes from "@/components/chefDeDepartement/notesManagement/PrintableNotes.vue";
import StudentEvaluationSheet from "@/components/chefDeDepartement/notesManagement/StudentEvaluationSheet.vue";

// Import PrimeVue components
import Toast from "primevue/toast";

// Component state
const notes = ref([]);
const filieres = ref([]);
const selectedFiliere = ref(null);
const loading = ref(false);
const searchQuery = ref("");
const showPrintDialog = ref(false);
const printableNotesRef = ref(null);
const isPrintingEvaluationSheet = ref(false);

// Print settings
const printSettings = ref({
    showDetails: true,
    showAllFilieres: false
});

// Student evaluation sheet state
const selectedEvaluationStudent = ref(null);
const selectedEvaluationNote = ref(null);
const selectedEvaluationDetail = ref(null);
const selectedEvaluationFiliereName = ref('');

// Services
const toast = useToast();

// State for grade percentages
const pourcentages = ref({
    pourcentageRapport: 40,
    pourcentageSoutenance: 40,
    pourcentageEncadrant: 20,
});

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

// Print functionality
function openPrintDialog() {
    showPrintDialog.value = true;
}

function printNotes(options) {
    // Set flag to indicate we're printing the summary table, not an evaluation sheet
    isPrintingEvaluationSheet.value = false;
    
    // Update print settings with dialog options
    printSettings.value = options;
    
    // Reset any evaluation sheet state
    selectedEvaluationStudent.value = null;
    selectedEvaluationNote.value = null;
    
    // Schedule print operation to occur after the DOM has been updated
    setTimeout(() => {
        window.print();
    }, 100);
}

async function printEvaluationSheet(data) {
    try {
        // Show loading state
        loading.value = true;
        
        // Set flag to indicate we're printing an evaluation sheet
        isPrintingEvaluationSheet.value = true;
        
        // Reset summary print settings
        printSettings.value = {
            showDetails: false,
            showAllFilieres: false
        };
        
        // Set student data
        selectedEvaluationStudent.value = data.student;
        selectedEvaluationNote.value = data.note;
        selectedEvaluationFiliereName.value = data.filiereName;
        
        // Fetch detailed note information for the student
        if (data.student && data.student.id) {
            try {
                const detailsResponse = await ApiService.get(
                    `/chef_de_departement/notes/student/${data.student.id}/details`
                );
                selectedEvaluationDetail.value = detailsResponse;
            } catch (error) {
                console.error("Error fetching note details:", error);
                selectedEvaluationDetail.value = {};
            }
        }
        
        // Schedule print after details have been loaded
        setTimeout(() => {
            window.print();
            
            // Reset state after printing
            setTimeout(() => {
                selectedEvaluationStudent.value = null;
                selectedEvaluationNote.value = null;
                selectedEvaluationDetail.value = null;
                isPrintingEvaluationSheet.value = false;
            }, 500);
        }, 300);
    } catch (error) {
        handleApiError(error, "Erreur lors de la préparation de l'impression");
    } finally {
        loading.value = false;
    }
}

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

/* Hide printable component in normal view */
.print-only {
    display: none;
}

/* Print styles */
@media print {
    body {
        margin: 0;
        padding: 0;
        background: white;
    }
    
    .no-print {
        display: none !important;
    }
    
    .print-only {
        display: block !important;
    }
    
    @page {
        size: A4;
        margin: 1cm;
    }
}
</style>