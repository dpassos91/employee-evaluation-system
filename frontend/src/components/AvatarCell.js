import { useState } from "react";
import { profileAPI } from "../api/profileAPI"; // Ajusta o import conforme necessário
import profileIcon from "../images/profile_icon.png"; // Ajusta o import conforme necessário

/**
 * AvatarCell component.
 *
 * Displays a user avatar image, falling back to a default icon if the image
 * is missing or fails to load. The avatar can be a profile photo from the API
 * or a local icon.
 *
 * @component
 * @param {Object} props - Component props.
 * @param {string} props.avatar - The filename or path of the user's avatar (optional).
 * @param {string} props.name - The user's display name (used for alt text).
 * @returns {JSX.Element} The avatar image element.
 *
 * @example
 * <AvatarCell avatar="user_photo.jpg" name="Jane Doe" />
 */
function AvatarCell({ avatar, name }) {
  const [src, setSrc] = useState(
    avatar && avatar.trim() !== ""
      ? profileAPI.getPhoto(avatar)
      : profileIcon
  );
  return (
    <img
      src={src}
      alt={name}
      className="w-8 h-8 rounded-full object-cover ml-12"
      onError={() => setSrc("/default_avatar.png")}
    />
  );
}

export default AvatarCell;