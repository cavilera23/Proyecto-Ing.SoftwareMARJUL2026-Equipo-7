import Swal from "sweetalert2";

const baseConfig = {
  background: "#1f2937",
  color: "#f9fafb",
  confirmButtonText: "Aceptar",
  customClass: {
    popup: "rounded-alert",
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
