<template>
    <aside v-if="isAuthenticated" class="app-sidebar">
        <div class="sidebar-content">
            <!--dynamic logo section -->
            <div class="sidebar-logo">
                <img
                    v-if="isDarkMode"
                    src="@/assets/logo/full_dark.svg"
                    alt="pfe management"
                />
                <img
                    v-else
                    src="@/assets/logo/full_light.svg"
                    alt="pfe management"
                />
            </div>

            <!-- dymanic menue for diferent user roles -->
            <div class="sidebar-menu">
                <div
                    v-for="(item, index) in menuItems"
                    :key="index"
                    class="menu-item"
                    :class="{ active: isActiveRoute(item.route) }"
                    @click="navigateTo(item.command)"
                >
                    <i :class="item.icon"></i>
                    <span>{{ item.label }}</span>
                    <i
                        v-if="item.hasSubmenu"
                        class="pi pi-chevron-down submenu-icon"
                    ></i>
                </div>
            </div>

            <!-- Logout button  -->
            <div class="sidebar-footer">
                <div class="menu-item logout-item" @click="logout">
                    <i class="pi pi-sign-out"></i>
                    <span>Déconnexion</span>
                </div>
            </div>
        </div>
    </aside>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import AuthService from "../services/AuthService";
import { ThemeService } from "@/services/ThemeService";

const router = useRouter();
const route = useRoute();
const isDarkMode = computed(() => ThemeService.getTheme() === "dark");

// listen for theme changes
onMounted(() => {
    window.addEventListener("themechange", (e) => {
        isDarkMode.value = e.detail.theme === "dark";
    });
});

// Add authentication state tracker
const authState = ref(AuthService.isAuthenticated());
const updateAuthState = () => {
    authState.value = AuthService.isAuthenticated();
};

// Auth state computed property
const isAuthenticated = computed(() => authState.value);

// Update user reactivity
const authUser = ref(AuthService.getCurrentUser());
const updateUser = () => {
    authUser.value = AuthService.getCurrentUser();
};
const user = computed(() => authUser.value);

// Listen for auth state changes
onMounted(() => {
    window.addEventListener("auth-state-changed", updateAuthState);
    window.addEventListener("auth-state-changed", updateUser);
});

// Clean up event listeners when component is unmounted
onUnmounted(() => {
    window.removeEventListener("auth-state-changed", updateAuthState);
    window.removeEventListener("auth-state-changed", updateUser);
});

// Logout function
const logout = () => {
    AuthService.logout();
    // Emit a custom event that components can listen to
    window.dispatchEvent(new CustomEvent("auth-state-changed"));
    router.push("/login");
};

// Navigation helper functions
const isActiveRoute = (routePath) => {
    if (!routePath) return false;
    return route.path === routePath || route.path.startsWith(routePath + "/");
};

const navigateTo = (commandFn) => {
    if (typeof commandFn === "function") {
        commandFn();
    }
};

// Menu items based on user role
const menuItems = computed(() => {
    const role = user.value?.role;

    // Default menu items for all users
    const items = [
        {
            label: "Tableau de bord",
            icon: "pi pi-th-large",
            command: () => router.push("/dashboard"),
            route: "/dashboard",
        },
    ];

    // Role-specific menu items
    if (role === "ETUDIANT") {
        items.push(
            {
                label: "Mon PFE",
                icon: "pi pi-file",
                command: () => router.push("/student/pfe"),
                route: "/student/pfe",
            },
            {
                label: "Documents",
                icon: "pi pi-file",
                command: () => router.push("/etudiant/documents"),
                route: "/etudiant/documents",
            },
            {
                label: "Rapport Final",
                icon: "pi pi-file-pdf",
                command: () => router.push("/etudiant/rapport"),
                route: "/etudiant/rapport",
            },
            {
                label: "Planning",
                icon: "pi pi-calendar",
                command: () => router.push("/student/schedule"),
                route: "/student/schedule",
            }
        );
    } else if (role === "ENCADRANT") {
        items.push(
            {
                label: "Mes Étudiants",
                icon: "pi pi-users",
                command: () => router.push("/supervisor/students"),
                route: "/supervisor/students",
            },
            {
                label: "Révision des documents",
                icon: "pi pi-check-square",
                command: () => router.push("/encadrant/document-evaluation"),
                route: "/encadrant/document-evaluation",
            },
            {
                label: "Planning",
                icon: "pi pi-calendar",
                command: () => router.push("/supervisor/schedule"),
                route: "/supervisor/schedule",
            },
            {
                label: "Mes Soutenances",
                icon: "pi pi-calendar-plus",
                command: () => router.push("/encadrant/soutenances"),
                route: "/encadrant/soutenances",
            }
        );
    } else if (role === "CHEF_DE_DEPARTEMENT") {
        items.push(
            {
                label: "Gestion des Comptes",
                icon: "pi pi-user",
                command: () => router.push("/management/comptes"),
                route: "/management/comptes",
            },
            {
                label: "Gestion des Binômes",
                icon: "pi pi-users",
                command: () => router.push("/management/binomes"),
                route: "/management/binomes",
            },
            {
                label: "Gestion des Sujets",
                icon: "pi pi-file-o",
                command: () => router.push("/management/sujets"),
                route: "/management/sujets",
            },
            {
                label: "Suggestions de Sujets",
                icon: "pi pi-inbox",
                command: () => router.push("/management/sujet-suggestions"),
                route: "/management/sujet-suggestions",
            },
            {
                label: "Gestion des Soutenances",
                icon: "pi pi-calendar",
                command: () => router.push("/management/soutenances"),
                route: "/management/soutenances",
            },
            {
                label: "Gestion des Notes",
                icon: "pi pi-chart-line",
                command: () => router.push("/management/notes-management"),
                route: "/management/notes-management",
            },
            {
                label: "Paramètres",
                icon: "pi pi-cog",
                command: () => router.push("/management/settings"),
                route: "/management/settings",
            }
        );
    } else if (role === "JURY") {
        items.push(
            {
                label: "Mes Soutenances",
                icon: "pi pi-calendar-plus",
                command: () => router.push("/jury/soutenances"),
                route: "/jury/soutenances",
            },
            {
                label: "Évaluation Soutenances",
                icon: "pi pi-microphone",
                command: () => router.push("/jury/grading"),
                route: "/jury/grading",
            },
            {
                label: "Évaluation Rapports",
                icon: "pi pi-file-pdf",
                command: () => router.push("/jury/report-evaluation"),
                route: "/jury/report-evaluation",
            }
        );
    }

    return items;
});
</script>

<style scoped>
.app-sidebar {
    width: 250px;
    background-color: #ffffff;
    border-right: 1px solid #f0f0f0;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 990;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 0;
    /* border-radius: 0 16px 16px 0; */
}

.dark-mode .app-sidebar {
    background-color: #1a1a1a;
    border-right: 1px solid #2c2c2c;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.15);
}

.sidebar-content {
    padding: 0;
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
}

/* Logo section styles */
.sidebar-logo {
    padding: 10px 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
}

.sidebar-logo img {
    height: auto;
    width: 100%;
}

.dark-mode .app-name {
    color: #f0f0f0;
}

/* Menu styles */
.sidebar-menu {
    padding: 10px 0;
    flex: 1;
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    cursor: pointer;
    color: #666;
    font-size: 14px;
    position: relative;
    transition: all 0.2s ease;
}

.menu-item:hover {
    background-color: #f8f9fa;
    color: var(--primary-color);
}

.dark-mode .menu-item:hover {
    background-color: #2a2a2a;
}

.menu-item.active {
    color: var(--primary-color);
    background-color: rgba(67, 97, 238, 0.1);
    font-weight: 500;
}

.dark-mode .menu-item.active {
    background-color: rgba(67, 97, 238, 0.15);
}

.menu-item.active::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    width: 4px;
    height: 100%;
    background-color: var(--primary-color);
    border-radius: 0 4px 4px 0;
}

.menu-item i {
    margin-right: 12px;
    font-size: 18px;
    width: 20px;
    text-align: center;
}

.menu-item span {
    flex: 1;
}

.submenu-icon {
    font-size: 12px !important;
    margin-right: 0 !important;
    opacity: 0.7;
}

/* Footer/Logout styles */
.sidebar-footer {
    margin-top: auto;
    padding: 10px 0;
    border-top: 1px solid #f0f0f0;
}

.dark-mode .sidebar-footer {
    border-top: 1px solid #2c2c2c;
}

.logout-item {
    color: #666;
}

.logout-item:hover {
    color: #f44336;
    background-color: rgba(244, 67, 54, 0.1);
}

.dark-mode .logout-item:hover {
    background-color: rgba(244, 67, 54, 0.15);
}

@media (max-width: 768px) {
    .app-sidebar {
        width: 100%;
        height: auto;
        position: relative;
        border-radius: 0;
        border-right: none;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    }

    .sidebar-content {
        height: auto;
    }

    .sidebar-logo {
        padding: 16px;
    }

    .menu-item {
        padding: 10px 16px;
    }
}
</style>
