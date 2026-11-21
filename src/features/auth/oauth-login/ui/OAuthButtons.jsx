import { API_URL } from "@/shared/config/env";
import { FacebookIcon, TelegramIcon, VkIcon } from "./icons";
import s from "./OAuthButtons.module.css";

const PROVIDERS = [
  { id: "vk", label: "ВКонтакте", Icon: VkIcon },
  { id: "facebook", label: "Facebook", Icon: FacebookIcon },
  { id: "telegram", label: "Telegram", Icon: TelegramIcon },
];

/**
 * Кнопки входа через соцсети. Бэкенд сам делает OAuth-редирект к провайдеру
 * и возвращает пользователя обратно на сайт.
 * TODO: согласовать с бэкендом адреса (в Spring Security по умолчанию /oauth2/authorization/{id}).
 */
export function OAuthButtons({ action = "Войти" }) {
  return (
    <ul className={s.list}>
      {PROVIDERS.map(({ id, label, Icon }) => (
        <li key={id}>
          <a href={`${API_URL}/auth/oauth/${id}`} className={s.button} aria-label={`${action} через ${label}`}>
            <Icon />
          </a>
        </li>
      ))}
    </ul>
  );
}
