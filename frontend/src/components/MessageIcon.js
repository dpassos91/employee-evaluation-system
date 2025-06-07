import messageIcon from "../images/message_icon.png"; // substitui pela tua imagem real

export default function MessageIcon() {
  return (
    <button
      aria-label="Mensagens"
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
