<template>
    <div class="etudiant-view">
        <Toast />
        <div class="view-container">
            <!-- Student has pending requests view -->
            <div v-if="hasPendingRequests" class="card">
                <div class="card-header">
                    <h1>Demandes de Binôme en Attente</h1>
                    <p>
                        Voici les demandes de binôme que vous avez reçues. Vous
                        pouvez les accepter ou les refuser.
                    </p>
                </div>
                <div class="card-content">
                    <div v-if="loading" class="loading-container">
                        <ProgressSpinner />
                        <p>Chargement des demandes...</p>
                    </div>
                    <div
                        v-else-if="pendingRequests.length === 0"
                        class="empty-state"
                    >
                        <i class="pi pi-users" style="font-size: 3rem"></i>
                        <h3>Aucune demande en attente</h3>
                        <p>Vous n'avez pas de demandes de binôme en attente.</p>
                        <Button
                            label="Choisir un Binôme"
                            icon="pi pi-users"
                            @click="goToChooseBinome"
                        />
                    </div>
                    <div v-else class="requests-container">
                        <div
                            v-for="request in pendingRequests"
                            :key="request.id"
                            class="request-card"
                        >
                            <div class="request-info">
                                <h3>
                                    {{ request.demandeurNom }}
                                    {{ request.demandeurPrenom }}
                                </h3>
                                <p>
                                    Demande envoyée le:
                                    {{ formatDate(request.dateDemande) }}
                                </p>
                            </div>
                            <div class="request-actions">
                                <Button
                                    icon="pi pi-check"
                                    class="p-button-success p-button-rounded"
                                    @click="acceptRequest(request.id)"
                                    :loading="
                                        processingRequestId === request.id &&
                                        processingAction === 'accept'
                                    "
                                    :disabled="isProcessingAnyRequest"
                                    tooltip="Accepter"
                                />
                                <Button
                                    icon="pi pi-times"
                                    class="p-button-danger p-button-rounded"
                                    @click="rejectRequest(request.id)"
                                    :loading="
                                        processingRequestId === request.id &&
                                        processingAction === 'reject'
                                    "
                                    :disabled="isProcessingAnyRequest"
                                    tooltip="Refuser"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Choose binome view -->
            <div v-else-if="showChooseBinome" class="card">
                <div class="card-header">
                    <h1>Choisir un Binôme</h1>
                    <p>
                        Choisissez un étudiant de votre filière pour former un
                        binôme, ou continuez seul.
                    </p>
                </div>
                <div class="card-content">
                    <div v-if="loading" class="loading-container">
                        <ProgressSpinner />
                        <p>Chargement des étudiants disponibles...</p>
                    </div>
                    <div v-else>
                        <div v-if="hasSentRequest" class="sent-request-info">
                            <i class="pi pi-clock"></i>
                            <h3>Demande en attente</h3>
                            <p>
                                Vous avez déjà envoyé une demande qui est en
                                attente de réponse.
                            </p>
                        </div>
                        <div
                            v-else-if="availableStudents.length === 0"
                            class="empty-state"
                        >
                            <i class="pi pi-users" style="font-size: 3rem"></i>
                            <h3>Aucun étudiant disponible</h3>
                            <p>
                                Il n'y a pas d'étudiants disponibles dans votre
                                filière. Vous pouvez continuer seul.
                            </p>
                        </div>
                        <div v-else class="students-container">
                            <div
                                v-for="student in availableStudents"
                                :key="student.id"
                                class="student-card"
                            >
                                <div class="student-info">
                                    <h3>
                                        {{ student.nom }} {{ student.prenom }}
                                    </h3>
                                    <p>{{ student.email }}</p>
                                </div>
                                <Button
                                    label="Envoyer une demande"
                                    icon="pi pi-send"
                                    @click="sendRequest(student.id)"
                                    :loading="
                                        processingStudentId === student.id
                                    "
                                    :disabled="isProcessingAnyStudent"
                                />
                            </div>
                        </div>
                        <div class="continue-alone-container">
                            <Divider>
                                <span>OU</span>
                            </Divider>
                            <Button
                                label="Continuer Seul"
                                icon="pi pi-user"
                                class="p-button-secondary continue-alone-btn"
                                @click="continueAlone"
                                :loading="processingAlone"
                                :disabled="
                                    isProcessingAnyStudent || processingAlone
                                "
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import ApiService from "@/services/ApiService";

// PrimeVue components
import Toast from "primevue/toast";
import Button from "primevue/button";
import ProgressSpinner from "primevue/progressspinner";
import Divider from "primevue/divider";

export default {
    name: "BinomeSelectionView",
    components: {
        Toast,
        Button,
        ProgressSpinner,
        Divider,
    },
    setup() {
        // Router and toast
        const router = useRouter();
        const toast = useToast();

        // State management
        const loading = ref(false);
        const hasPendingRequests = ref(false);
        const pendingRequests = ref([]);
        const showChooseBinome = ref(false);
        const availableStudents = ref([]);
        const hasSentRequest = ref(false);
        const targetStudentId = ref(null);
        const hasBinome = ref(false);

        // Processing states
        const processingRequestId = ref(null);
        const processingStudentId = ref(null);
        const processingAction = ref(null);
        const processingAlone = ref(false);

        // Computed properties
        const isProcessingAnyRequest = computed(
            () => processingRequestId.value !== null
        );
        const isProcessingAnyStudent = computed(
            () => processingStudentId.value !== null
        );

        // Check student status on mount
        onMounted(async () => {
            await checkStudentStatus();
        });

        // Format date function for displaying request dates
        const formatDate = (dateString) => {
            if (!dateString) return "";
            const date = new Date(dateString);
            return new Intl.DateTimeFormat("fr-FR", {
                year: "numeric",
                month: "long",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
            }).format(date);
        };

        // Check student's current status
        const checkStudentStatus = async () => {
            loading.value = true;
            try {
                const status = await ApiService.get("/etudiant/status");

                // Check if student already has a binome
                if (status.hasBinome) {
                    hasBinome.value = true;

                    // If has binome but no sujet, go to sujet selection
                    if (!status.hasSujet) {
                        router.push("/etudiant/sujet");
                    } else {
                        // Go to dashboard or other student view if has both binome and sujet
                        router.push("/dashboard");
                    }
                    return;
                }

                // Check for pending requests received
                if (status.hasPendingBinomeRequests) {
                    hasPendingRequests.value = true;
                    await loadPendingRequests();
                } else {
                    // No pending requests, show choose binome view
                    hasPendingRequests.value = false;
                    showChooseBinome.value = true;

                    // Check if student already sent a request
                    hasSentRequest.value = status.hasSentBinomeRequest;

                    // Load available students if no request sent
                    if (!hasSentRequest.value) {
                        await loadAvailableStudents();
                    }
                }
            } catch (error) {
                handleError(error, "Erreur lors de la vérification du statut");
            } finally {
                loading.value = false;
            }
        };

        // Load pending requests received by the student
        const loadPendingRequests = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/etudiant/binome/requests"
                );
                pendingRequests.value = response;
            } catch (error) {
                handleError(error, "Erreur lors du chargement des demandes");
            } finally {
                loading.value = false;
            }
        };

        // Load available students for binome formation
        const loadAvailableStudents = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/etudiant/binome/available-students"
                );
                availableStudents.value = response.availableStudents || [];
                hasSentRequest.value = response.hasSentRequest || false;
                targetStudentId.value = response.targetStudentId;
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors du chargement des étudiants disponibles"
                );
            } finally {
                loading.value = false;
            }
        };

        // Accept a binome request
        const acceptRequest = async (requestId) => {
            processingRequestId.value = requestId;
            processingAction.value = "accept";
            try {
                const response = await ApiService.post(
                    `/etudiant/binome/accept/${requestId}`
                );

                toast.add({
                    severity: "success",
                    summary: "Demande acceptée",
                    detail: "Vous avez accepté la demande de binôme",
                    life: 3000,
                });

                // Binome is now formed, redirect to sujet selection
                setTimeout(() => {
                    router.push("/etudiant/sujet");
                }, 1500);
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors de l'acceptation de la demande"
                );
            } finally {
                processingRequestId.value = null;
                processingAction.value = null;
            }
        };

        // Reject a binome request
        const rejectRequest = async (requestId) => {
            processingRequestId.value = requestId;
            processingAction.value = "reject";
            try {
                await ApiService.post(
                    `/etudiant/binome/reject/${requestId}`
                );

                // Remove from local list
                pendingRequests.value = pendingRequests.value.filter(
                    (request) => request.id !== requestId
                );

                toast.add({
                    severity: "info",
                    summary: "Demande refusée",
                    detail: "Vous avez refusé la demande de binôme",
                    life: 3000,
                });

                // If no more pending requests, show choose binome view
                if (pendingRequests.value.length === 0) {
                    hasPendingRequests.value = false;
                    showChooseBinome.value = true;
                    await loadAvailableStudents();
                }
            } catch (error) {
                handleError(error, "Erreur lors du refus de la demande");
            } finally {
                processingRequestId.value = null;
                processingAction.value = null;
            }
        };

        // Send a binome request to another student
        const sendRequest = async (studentId) => {
            processingStudentId.value = studentId;
            try {
                const response = await ApiService.post(
                    "/etudiant/binome/request",
                    studentId
                );

                if (response.success) {
                    toast.add({
                        severity: "success",
                        summary: "Demande envoyée",
                        detail:
                            response.message ||
                            "Votre demande de binôme a été envoyée",
                        life: 3000,
                    });

                    // If auto-accepted (there was a mutual request)
                    if (response.binome) {
                        toast.add({
                            severity: "info",
                            summary: "Binôme formé",
                            detail: "Une demande mutuelle existait. Votre binôme a été formé automatiquement",
                            life: 4000,
                        });

                        // Redirect to sujet selection
                        setTimeout(() => {
                            router.push("/etudiant/sujet");
                        }, 2000);
                    } else {
                        // Just update sent request status
                        hasSentRequest.value = true;
                    }
                }
            } catch (error) {
                handleError(error, "Erreur lors de l'envoi de la demande");
            } finally {
                processingStudentId.value = null;
            }
        };

        // Continue alone (create single-student binome)
        const continueAlone = async () => {
            processingAlone.value = true;
            try {
                const response = await ApiService.post(
                    "/etudiant/binome/continue-alone"
                );

                toast.add({
                    severity: "success",
                    summary: "Binôme créé",
                    detail: "Vous avez choisi de continuer seul. Votre binôme a été créé",
                    life: 3000,
                });

                // Redirect to sujet selection
                setTimeout(() => {
                    router.push("/etudiant/sujet");
                }, 1500);
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors de la création du binôme individuel"
                );
            } finally {
                processingAlone.value = false;
            }
        };

        // Go to choose binome view (from pending requests)
        const goToChooseBinome = () => {
            hasPendingRequests.value = false;
            showChooseBinome.value = true;
            loadAvailableStudents();
        };

        // Error handling
        const handleError = (error, defaultMessage) => {
            console.error(defaultMessage, error);
            let message = defaultMessage;

            if (error.message) {
                message = error.message;
            } else if (
                error.response &&
                error.response.data &&
                error.response.data.message
            ) {
                message = error.response.data.message;
            }

            toast.add({
                severity: "error",
                summary: "Erreur",
                detail: message,
                life: 5000,
            });
        };

        return {
            // State
            loading,
            hasPendingRequests,
            pendingRequests,
            showChooseBinome,
            availableStudents,
            hasSentRequest,
            processingRequestId,
            processingStudentId,
            processingAction,
            processingAlone,
            isProcessingAnyRequest,
            isProcessingAnyStudent,

            // Methods
            formatDate,
            acceptRequest,
            rejectRequest,
            sendRequest,
            continueAlone,
            goToChooseBinome,
        };
    },
};
</script>

<style scoped>
.etudiant-view {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.view-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.card {
    background-color: var(--surface-card);
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.card-header {
    padding: 24px;
    background-color: var(--primary-color);
    color: white;
}

.card-header h1 {
    margin: 0 0 8px;
    font-size: 1.75rem;
}

.card-header p {
    margin: 0;
    opacity: 0.9;
}

.card-content {
    padding: 24px;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
}

.loading-container p {
    margin-top: 16px;
    color: var(--text-color-secondary);
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 40px 20px;
}

.empty-state i {
    color: var(--primary-color);
    opacity: 0.6;
    margin-bottom: 16px;
}

.empty-state h3 {
    margin: 0 0 8px;
    color: var(--text-color);
}

.empty-state p {
    margin: 0 0 24px;
    color: var(--text-color-secondary);
}

.requests-container,
.students-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.request-card,
.student-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background-color: var(--surface-hover);
    border-radius: 8px;
    transition: background-color 0.2s;
}

.request-card:hover,
.student-card:hover {
    background-color: var(--surface-hover);
}

.request-info,
.student-info {
    flex: 1;
}

.request-info h3,
.student-info h3 {
    margin: 0 0 4px;
    font-size: 1.1rem;
    color: var(--text-color);
}

.request-info p,
.student-info p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
}

.request-actions {
    display: flex;
    gap: 8px;
}

.sent-request-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 30px;
    background-color: var(--surface-hover);
    border-radius: 8px;
    margin-bottom: 20px;
}

.sent-request-info i {
    font-size: 2rem;
    color: var(--primary-color);
    margin-bottom: 16px;
}

.sent-request-info h3 {
    margin: 0 0 8px;
}

.sent-request-info p {
    margin: 0;
    color: var(--text-color-secondary);
}

.continue-alone-container {
    margin-top: 32px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.continue-alone-btn {
    margin-top: 16px;
    min-width: 200px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .request-card,
    .student-card {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
    }

    .request-actions {
        align-self: flex-end;
    }
}
</style>
