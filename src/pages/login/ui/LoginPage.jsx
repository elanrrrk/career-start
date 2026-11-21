import { Link, useNavigate } from "react-router";
import { AuthPanel } from "@/widgets/auth-panel";
import { LoginForm } from "@/features/auth/login-by-email";
import { OAuthButtons } from "@/features/auth/oauth-login";
import { ROUTES } from "@/shared/config/routes";

export function LoginPage() {
  const navigate = useNavigate();

  return (
    <AuthPanel
      title="Вход"
      subtitle="Войти с помощью аккаунта в социальной сети"
      social={<OAuthButtons action="Войти" />}
      footer={<Link to={ROUTES.register}>Зарегистрироваться</Link>}
    >
      <LoginForm onSuccess={() => navigate(ROUTES.home)} />
    </AuthPanel>
  );
}
