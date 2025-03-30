<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Modifier le Sujet"
        :modal="true"
        class="sujet-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div v-if="sujet" class="field-container">
            <div class="p-field">
                <label for="edit-titre">Titre</label>
                <InputText
                    id="edit-titre"
                    v-model="formData.titre"
                    class="w-full"
                    placeholder="Titre du sujet"
                    required
                />
            </div>

            <div class="p-field">
                <label for="edit-theme">Thème</label>
                <InputText
                    id="edit-theme"
                    v-model="formData.theme"
                    class="w-full"
                    placeholder="Thème du sujet"
                    required
                />
            </div>

            <div class="p-field">
                <label for="edit-description">Description</label>
                <Textarea
                    id="edit-description"
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
                label="Sauvegarder"
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
    sujet: {
        type: Object,
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
});

// Watch for changes in sujet to update form
watch(
    () => props.sujet,
    (newSujet) => {
        if (newSujet) {
            formData.value = {
                id: newSujet.id,
                titre: newSujet.titre || "",
                theme: newSujet.theme || "",
                description: newSujet.description || "",
            };
        }
    },
    { immediate: true }
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
