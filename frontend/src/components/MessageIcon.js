import messageIcon from "../images/message_icon.png"; 
import { useNavigate } from "react-router-dom";

export default function MessageIcon() {
  const navigate = useNavigate();
  return (
    <button
      type="button"
      onClick={() => navigate("/chat")} 
      className="focus:outline-none"
    >
      <img
        src={messageIcon}
        alt="Mensagens"
        className="w-12 h-12 md:w-14 md:h-14"
      />
    </button>
  );
}
