<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Ajouter un Nouveau Sujet"
        :modal="true"
        class="sujet-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div class="field-container">
            <div class="p-field">
                <label for="titre">Titre</label>
                <InputText
                    id="titre"
                    v-model="formData.titre"
                    class="w-full"
                    placeholder="Titre du sujet"
                    required
                />
            </div>

            <div class="p-field">
                <label for="theme">Thème</label>
                <InputText
                    id="theme"
                    v-model="formData.theme"
                    class="w-full"
                    placeholder="Thème du sujet"
                    required
                />
            </div>

            <div class="p-field">
                <label for="description">Description</label>
                <Textarea
                    id="description"
                    v-model="formData.description"
                    rows="5"
                    class="w-full"
                    placeholder="Description détaillée du sujet"
                    required
                    autoResize
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
                @click="$emit('submit', formData)"
                :loading="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { ref, watch } from "vue";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    selectedClass: {
        type: [Number, String, null],
        default: null,
    },
    submitting: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(["update:visible", "submit", "cancel"]);

// Form data
const formData = ref({
    titre: "",
    theme: "",
    description: "",
    filiereId: null,
});

// Watch for changes in selectedClass to update formData.filiereId
watch(
    () => props.selectedClass,
    (newValue) => {
        if (newValue) {
            formData.value.filiereId = newValue;
        }
    },
    { immediate: true }
);

// Reset form when modal becomes visible
watch(
    () => props.visible,
    (newVisible) => {
        if (newVisible) {
            formData.value = {
                titre: "",
                theme: "",
                description: "",
                filiereId: props.selectedClass,
            };
        }
    }
);
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
</style>
