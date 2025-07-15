import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useIntl } from "react-intl";
import { authAPI } from "../api/authAPI";

export default function ConfirmAccountPage() {
    console.log("ConfirmAccountPage MONTADO!");
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { formatMessage } = useIntl();

  useEffect(() => {
    const confirmToken = searchParams.get("confirmToken");

    if (!confirmToken) {
      // Token em falta, feedback de erro
      sessionStorage.setItem(
        "accountConfirmToast",
        JSON.stringify({
          type: "error",
          message: formatMessage({
            id: "confirm.error.noToken",
            defaultMessage: "O link de confirmação não é válido ou já expirou."
          })
        })
      );
      navigate("/login", { replace: true });
      return;
    }

    authAPI.confirmAccount(confirmToken)
      .then(res => {
        sessionStorage.setItem(
          "accountConfirmToast",
          JSON.stringify({
            type: "success",
            message: res.message || formatMessage({
              id: "confirm.success",
              defaultMessage: "Conta confirmada com sucesso! Pode agora fazer login."
            })
          })
        );
        navigate("/login", { replace: true });
      })
      .catch(err => {
        const backendError = err?.response?.data || err;
        sessionStorage.setItem(
          "accountConfirmToast",
          JSON.stringify({
            type: "error",
            message:
              backendError.message ||
              formatMessage({
                id: "confirm.error.generic",
                defaultMessage: "Não foi possível confirmar a conta. O link pode ter expirado."
              })
          })
        );
        navigate("/login", { replace: true });
      });
  }, [searchParams, formatMessage, navigate]);

  return null;
}



