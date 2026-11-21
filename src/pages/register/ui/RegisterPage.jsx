import { Link, useNavigate } from "react-router";
import { AuthPanel } from "@/widgets/auth-panel";
import { RegisterForm } from "@/features/auth/register-by-email";
import { OAuthButtons } from "@/features/auth/oauth-login";
import { ROUTES } from "@/shared/config/routes";

export function RegisterPage() {
  const navigate = useNavigate();

  return (
    <AuthPanel
      title="Регистрация"
      subtitle="Зарегистрироваться с помощью аккаунта в социальной сети"
      social={<OAuthButtons action="Зарегистрироваться" />}
      footer={<Link to={ROUTES.login}>Войти</Link>}
    >
      <RegisterForm onSuccess={() => navigate(ROUTES.home)} />
    </AuthPanel>
  );
}
