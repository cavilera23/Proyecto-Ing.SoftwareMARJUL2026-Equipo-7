import Swal from "sweetalert2";

const baseConfig = {
  background: "#ffffff",
  color: "#5a6675",
  confirmButtonText: "Aceptar",
  buttonsStyling: true,
  customClass: {
    popup: "cr-swal",
    title: "cr-swal-title",
    confirmButton: "cr-swal-confirm",
    cancelButton: "cr-swal-cancel",
  },
};

export const showSuccessAlert = (title, text) => {
  return Swal.fire({
    ...baseConfig,
    icon: "success",
    title,
    text,
    confirmButtonColor: "#10b981",
  });
};

export const showErrorAlert = (title, text) => {
  return Swal.fire({
    ...baseConfig,
    icon: "error",
    title,
    text,
    confirmButtonText: "Entendido",
    confirmButtonColor: "#ef4444",
  });
};

export const showWarningAlert = (title, text) => {
  return Swal.fire({
    ...baseConfig,
    icon: "warning",
    title,
    text,
    confirmButtonText: "Entendido",
    confirmButtonColor: "#f59e0b",
  });
};

export const showInfoAlert = (title, text) => {
  return Swal.fire({
    ...baseConfig,
    icon: "info",
    title,
    text,
    confirmButtonColor: "#3b82f6",
  });
};

export const showConfirmAlert = (title, text) => {
  return Swal.fire({
    ...baseConfig,
    icon: "warning",
    title,
    text,
    showCancelButton: true,
    confirmButtonText: "Sí, continuar",
    cancelButtonText: "Cancelar",
    confirmButtonColor: "#ef4444",
    cancelButtonColor: "#6b7280",
  });
};
