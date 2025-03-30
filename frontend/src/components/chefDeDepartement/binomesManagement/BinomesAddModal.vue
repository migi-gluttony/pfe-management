<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Ajouter un Nouveau Binôme"
        :modal="true"
        class="binome-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div class="field-container">
            <div class="p-field">
                <label for="etudiant1"
                    >Étudiant 1 <span class="required">*</span></label
                >
                <Dropdown
                    id="etudiant1"
                    v-model="formData.etudiant1Id"
                    :options="processedStudents"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un étudiant"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                    @change="handleStudent1Change"
                />
            </div>

            <div class="p-field">
                <label for="etudiant2">Étudiant 2</label>
                <Dropdown
                    id="etudiant2"
                    v-model="formData.etudiant2Id"
                    :options="filteredStudent2Options"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un étudiant (optionnel)"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    :disabled="!formData.etudiant1Id"
                />
            </div>

            <div class="p-field">
                <label for="encadrant"
                    >Encadrant <span class="required">*</span></label
                >
                <Dropdown
                    id="encadrant"
                    v-model="formData.encadrantId"
                    :options="processedEncadrants"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un encadrant"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                />
            </div>

            <div class="p-field">
                <label for="sujet">Sujet <span class="required">*</span></label>
                <Dropdown
                    id="sujet"
                    v-model="formData.sujetId"
                    :options="processedSujets"
                    optionLabel="titre"
                    optionValue="id"
                    placeholder="Sélectionner un sujet"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                />
            </div>
        </div>

        <template #footer>
            <Button
                label="Annuler"
                icon="pi pi-times"
                class="p-button-text"
                @click="$emit('cancel')"
            />
            <Button
                label="Ajouter"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    selectedFiliere: {
        type: Number,
        required: true,
    },
    filieres: {
        type: Array,
        default: () => [],
    },
    availableStudents: {
        type: Array,
        default: () => [],
    },
    encadrants: {
        type: Array,
        default: () => [],
    },
    availableSujets: {
        type: Array,
        default: () => [],
    },
    submitting: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(["update:visible", "submit", "cancel"]);

// Form data
const formData = ref({
    etudiant1Id: null,
    etudiant2Id: null,
    encadrantId: null,
    sujetId: null,
});

// Get the selected filiere object
const selectedFiliereObj = computed(() => {
    return props.filieres.find((f) => f.id === props.selectedFiliere) || null;
});

// Process and filter students by filiere ID
const processedStudents = computed(() => {
    console.log("Available students:", props.availableStudents);
    console.log("Selected filière:", props.selectedFiliere);

    // Get binome student IDs to filter out students already in a binome
    const studentsInBinomes = new Set();

    // Filter students by filiere
    return props.availableStudents
        .filter((student) => {
            // We need to find the student's filiere
            // Try to determine the student's filiere by various properties
            if (student.filiereId) {
                return student.filiereId === props.selectedFiliere;
            }

            const filiereName = selectedFiliereObj.value?.nom;
            if (student.filiereName && filiereName) {
                return student.filiereName === filiereName;
            }

            // If we can't definitively tell the student's filiere,
            // include them for now
            return true;
        })
        .filter((student) => !studentsInBinomes.has(student.id))
        .map((student) => ({
            ...student,
            fullName: `${student.nom} ${student.prenom}`,
            id: student.id,
        }));
});

// Process encadrants for the dropdown display
const processedEncadrants = computed(() => {
    return props.encadrants.map((encadrant) => ({
        ...encadrant,
        fullName: `${encadrant.nom} ${encadrant.prenom}`,
    }));
});

// Process and filter subjects by the selected filiere ID
const processedSujets = computed(() => {
    console.log("Available sujets:", props.availableSujets);
    console.log("Selected filière ID:", props.selectedFiliere);

    return props.availableSujets.filter((sujet) => {
        // Try to determine if the subject is for the selected filiere
        if (sujet.filiereId) {
            return sujet.filiereId === props.selectedFiliere;
        }

        // If we can't determine the subject's filiere, include it
        return true;
    });
});

// Filtered options for student 2 - depends on student 1 selection
const filteredStudent2Options = computed(() => {
    if (!formData.value.etudiant1Id) return [];
    return processedStudents.value.filter(
        (s) => s.id !== formData.value.etudiant1Id
    );
});

// Reset the form when modal is opened/closed
watch(
    () => props.visible,
    (newValue) => {
        if (newValue) {
            // When modal opens, reset form
            formData.value = {
                etudiant1Id: null,
                etudiant2Id: null,
                encadrantId: null,
                sujetId: null,
            };
        }
    }
);

// Handle student 1 selection change
function handleStudent1Change() {
    // Reset student 2 if student 1 changes
    formData.value.etudiant2Id = null;
}

// Submit the form
function submitForm() {
    emit("submit", { ...formData.value });
}
</script>

<style scoped>
.field-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.p-field {
    margin-bottom: 0;
}

.p-field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required {
    color: red;
}

:deep(.p-dropdown) {
    width: 100%;
}
</style>
