import { useNavigate } from "react-router-dom";
import messageIcon from "../images/message_icon.png"; // Usa o teu ícone ou qualquer outro SVG

export default function MessageUserButton({ userId, disabled }) {
  const navigate = useNavigate();

  const handleClick = (e) => {
    e.stopPropagation(); // Se estiver dentro de um <tr> clickable, evita efeitos colaterais
    navigate(`/chat?user=${userId}`);
  };

  return (
    <button
      type="button"
      className="mx-auto flex items-center justify-center w-9 h-9 rounded-full hover:bg-gray-200 transition"
      title="Enviar mensagem"
      onClick={handleClick}
      disabled={disabled}
    >
      <img src={messageIcon} alt="Mensagem" className="w-8 h-8" />
    </button>
  );
}