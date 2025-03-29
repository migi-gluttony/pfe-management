<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Modifier le Compte"
        :modal="true"
        class="compte-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div v-if="compte" class="field-container">
            <div class="current-info">
                <p><strong>Rôle:</strong> {{ getRoleLabel(compte.role) }}</p>
                <p><strong>Email:</strong> {{ compte.email }}</p>
            </div>

            <div class="p-field">
                <label for="edit-nom"
                    >Nom <span class="required">*</span></label
                >
                <InputText
                    id="edit-nom"
                    v-model="formData.nom"
                    class="w-full"
                    placeholder="Nom de famille"
                    required
                />
            </div>

            <div class="p-field">
                <label for="edit-prenom"
                    >Prénom <span class="required">*</span></label
                >
                <InputText
                    id="edit-prenom"
                    v-model="formData.prenom"
                    class="w-full"
                    placeholder="Prénom"
                    required
                />
            </div>

            <div v-if="compte.role === 'ETUDIANT'" class="p-field">
                <label for="edit-cne"
                    >CNE <span class="required">*</span></label
                >
                <InputText
                    id="edit-cne"
                    v-model="formData.cne"
                    class="w-full"
                    placeholder="CNE"
                    required
                />
            </div>

            <div v-if="compte.role !== 'ETUDIANT'" class="p-field">
                <label for="edit-cni"
                    >CNI <span class="required">*</span></label
                >
                <InputText
                    id="edit-cni"
                    v-model="formData.cni"
                    class="w-full"
                    placeholder="CNI"
                    required
                />
            </div>

            <div class="p-field">
                <label for="edit-dateNaissance"
                    >Date de naissance <span class="required">*</span></label
                >
                <Calendar
                    id="edit-dateNaissance"
                    v-model="formData.dateNaissance"
                    :showIcon="true"
                    dateFormat="dd/mm/yy"
                    class="w-full"
                    placeholder="JJ/MM/AAAA"
                    required
                />
            </div>

            <div v-if="compte.role === 'ETUDIANT'" class="p-field">
                <label for="edit-filiereId"
                    >Filière <span class="required">*</span></label
                >
                <Dropdown
                    id="edit-filiereId"
                    v-model="formData.filiereId"
                    :options="filieres"
                    optionLabel="nom"
                    optionValue="id"
                    placeholder="Sélectionner une filière"
                    class="w-full"
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
                label="Sauvegarder"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { reactive, watch } from "vue";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import InputText from "primevue/inputtext";
import Calendar from "primevue/calendar";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    compte: {
        type: Object,
        default: null,
    },
    filieres: {
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
const formData = reactive({
    nom: "",
    prenom: "",
    cni: "",
    cne: "",
    dateNaissance: null,
    filiereId: null,
});

// Watch for changes in the compte prop to update the form
watch(
    () => props.compte,
    (newValue) => {
        if (newValue) {
            updateFormData(newValue);
        }
    },
    { immediate: true }
);

// Helper methods
function getRoleLabel(role) {
    const roleMap = {
        ETUDIANT: "Étudiant",
        ENCADRANT: "Encadrant",
        JURY: "Jury",
        ADMIN: "Admin",
        CHEF_DE_DEPARTEMENT: "Chef de Département",
    };
    return roleMap[role] || role;
}

function updateFormData(compte) {
    // Parse date if it's a string
    let dateNaissance = null;
    if (compte.dateNaissance) {
        try {
            dateNaissance =
                typeof compte.dateNaissance === "string"
                    ? new Date(compte.dateNaissance)
                    : compte.dateNaissance;

            // Check if date is valid
            if (isNaN(dateNaissance.getTime())) {
                dateNaissance = null;
            }
        } catch (error) {
            console.error("Error parsing date:", error);
            dateNaissance = null;
        }
    }

    // Find the filiere ID based on filiere name
    let filiereId = null;
    if (
        compte.role === "ETUDIANT" &&
        compte.filiereName &&
        props.filieres.length > 0
    ) {
        const filiere = props.filieres.find(
            (f) => f.nom === compte.filiereName
        );
        if (filiere) {
            filiereId = filiere.id;
        }
    }

    // Update form data
    Object.assign(formData, {
        nom: compte.nom || "",
        prenom: compte.prenom || "",
        cni: compte.cni || "",
        cne: compte.cne || "",
        dateNaissance: dateNaissance,
        filiereId: filiereId,
    });
}

function submitForm() {
    // Create a new object to avoid reactivity issues
    const formDataCopy = { ...formData };
    emit("submit", formDataCopy);
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

.current-info {
    background-color: var(--surface-ground);
    padding: 1rem;
    border-radius: 6px;
    margin-bottom: 1rem;
}

.current-info p {
    margin: 0.5rem 0;
}

:deep(.p-calendar) {
    width: 100%;
}

:deep(.p-dropdown) {
    width: 100%;
}
</style>
