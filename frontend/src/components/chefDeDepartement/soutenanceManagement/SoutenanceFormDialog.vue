<template>
    <Dialog
        v-model:visible="showModalModel"
        :header="
            soutenanceData?.id
                ? 'Modifier la Soutenance'
                : 'Programmer une Soutenance'
        "
        :modal="true"
        class="soutenance-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div
            v-if="soutenanceData && soutenanceData.binome"
            class="field-container"
        >
            <!-- Rest of your template with proper null checking -->
            <div class="binome-info">
                <h3>Binôme</h3>
                <p>
                    <strong>Étudiant 1:</strong>
                    {{ getStudentName(soutenanceData.binome?.etudiant1) }}
                </p>
                <!-- Use optional chaining everywhere -->
                <p v-if="soutenanceData.binome?.etudiant2">
                    <strong>Étudiant 2:</strong>
                    {{ getStudentName(soutenanceData.binome?.etudiant2) }}
                </p>
                <p>
                    <strong>Encadrant:</strong>
                    {{ getEncadrantName(soutenanceData.binome?.encadrant) }}
                </p>
                <p>
                    <strong>Sujet:</strong>
                    {{ soutenanceData.binome?.sujet?.titre || "-" }}
                </p>
                <p>
                    <strong>Filière:</strong>
                    {{ soutenanceData.binome?.filiereName || "-" }}
                </p>
            </div>

            <div class="p-field">
                <label for="date">Date <span class="required">*</span></label>
                <Calendar
                    id="date"
                    v-model="form.date"
                    dateFormat="dd/mm/yy"
                    class="w-full"
                    placeholder="Date de soutenance"
                    required
                    :minDate="minDate"
                    @date-select="validateForm"
                />
            </div>

            <div class="p-field">
                <label for="heure">Heure <span class="required">*</span></label>
                <Calendar
                    id="heure"
                    v-model="form.heure"
                    timeOnly
                    hourFormat="24"
                    class="w-full"
                    placeholder="Heure de soutenance"
                    required
                    @date-select="validateForm"
                />
            </div>

            <div class="p-field">
                <label for="salle">Salle <span class="required">*</span></label>
                <Dropdown
                    id="salle"
                    v-model="form.salleId"
                    :options="salles"
                    optionLabel="nom"
                    optionValue="id"
                    placeholder="Sélectionner une salle"
                    class="w-full"
                    required
                    @change="validateForm"
                />
                <small v-if="validationErrors.salleId" class="p-error">{{
                    validationErrors.salleId
                }}</small>
            </div>

            <div class="p-field">
                <label for="jury1"
                    >Jury 1 <span class="required">*</span></label
                >
                <Dropdown
                    id="jury1"
                    v-model="form.jury1Id"
                    :options="jurys"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner le premier membre du jury"
                    class="w-full"
                    required
                    :filter="true"
                    filterPlaceholder="Rechercher un membre du jury"
                    @change="validateForm"
                />
                <small v-if="validationErrors.jury1Id" class="p-error">{{
                    validationErrors.jury1Id
                }}</small>
            </div>

            <div class="p-field">
                <label for="jury2"
                    >Jury 2 <span class="required">*</span></label
                >
                <Dropdown
                    id="jury2"
                    v-model="form.jury2Id"
                    :options="filteredJury2Options"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner le second membre du jury"
                    class="w-full"
                    required
                    :filter="true"
                    filterPlaceholder="Rechercher un membre du jury"
                    @change="validateForm"
                />
                <small v-if="validationErrors.jury2Id" class="p-error">{{
                    validationErrors.jury2Id
                }}</small>
            </div>
        </div>

        <template #footer>
            <Button
                label="Annuler"
                icon="pi pi-times"
                class="p-button-text"
                @click="$emit('close')"
            />
            <Button
                label="Enregistrer"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
                :disabled="!isFormValid"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import Dialog from "primevue/dialog";
import Button from "primevue/button";
import Calendar from "primevue/calendar";
import Dropdown from "primevue/dropdown";

const props = defineProps({
    show: {
        type: Boolean,
        default: false,
    },
    soutenanceData: {
        type: Object,
        default: () => ({}),
    },
    salles: {
        type: Array,
        default: () => [],
    },
    jurys: {
        type: Array,
        default: () => [],
    },
    submitting: {
        type: Boolean,
        default: false,
    },
    validationErrors: {
        type: Object,
        default: () => ({}),
    },
});

const emit = defineEmits(["update:show", "save", "validate", "close"]);

// Two-way binding for modal visibility
const showModalModel = computed({
    get: () => props.show,
    set: (value) => emit("update:show", value),
});

// Minimum date for the calendar (today)
const minDate = ref(new Date());

// Form data
const form = ref({
    id: null,
    date: null,
    heure: null,
    binomeId: null,
    salleId: null,
    jury1Id: null,
    jury2Id: null,
});

// Filter jury2 options to exclude the selected jury1
const filteredJury2Options = computed(() => {
    return props.jurys.filter((j) => j.id !== form.value.jury1Id);
});

// Check if form is valid
const isFormValid = computed(() => {
    return (
        Object.keys(props.validationErrors).length === 0 &&
        form.value.date &&
        form.value.heure &&
        form.value.binomeId &&
        form.value.salleId &&
        form.value.jury1Id &&
        form.value.jury2Id
    );
});

// Watch for changes in soutenanceData and update the form
watch(
    () => props.soutenanceData,
    (newData) => {
        if (newData?.binome) {
            initializeForm(newData);
        }
    },
    { immediate: true, deep: true }
);

// Initialize the form with data
function initializeForm(data) {
    // Extract date and time
    let dateObj = new Date();
    if (data.date) {
        try {
            dateObj = new Date(data.date);
        } catch (e) {
            console.error("Error parsing date:", e);
        }
    }

    let timeObj = new Date();
    if (data.heure) {
        try {
            if (typeof data.heure === "string" && data.heure.includes(":")) {
                const [hours, minutes] = data.heure.split(":");
                timeObj = new Date();
                timeObj.setHours(parseInt(hours, 10), parseInt(minutes, 10));
            } else {
                timeObj = new Date(data.heure);
            }
        } catch (e) {
            console.error("Error parsing time:", e);
        }
    }

    // Initialize form with the data
    form.value = {
        id: data.id || null,
        date: dateObj,
        heure: timeObj,
        binomeId: data.binome?.id || null,
        salleId: data.salle?.id || null,
        jury1Id: data.jury1?.id || null,
        jury2Id: data.jury2?.id || null,
    };

    // Validate the form after initialization
    validateForm();
}

// Validate the form
function validateForm() {
    emit("validate", form.value);
}

// Submit the form
function submitForm() {
    emit("save", form.value);
}

// Helper methods
function getStudentName(student) {
    if (!student) return "N/A";
    return `${student.prenom} ${student.nom}`;
}

function getEncadrantName(encadrant) {
    if (!encadrant) return "N/A";
    return `${encadrant.prenom} ${encadrant.nom}`;
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

.binome-info {
    background-color: var(--surface-ground);
    padding: 1rem;
    border-radius: 6px;
    margin-bottom: 0.5rem;
}

.binome-info h3 {
    margin-top: 0;
    margin-bottom: 0.5rem;
    font-size: 1.1rem;
    color: var(--primary-color);
}

.binome-info p {
    margin: 0.3rem 0;
}

.p-error {
    display: block;
    margin-top: 0.25rem;
}
</style>
