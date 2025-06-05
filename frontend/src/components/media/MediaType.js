import { useEffect, useState } from "react";
import { userStore } from "../../stores/userStore";

function MediaType() {
  const updateMediatype = userStore((state) => state.updateMediatype);

  const [mediaType, setMediaType] = useState({
    isDesktopOrLaptop: false,
    isBigScreen: false,
    isTabletOrMobile: false,
    isPortrait: false,
    isRetina: false,
  });

  const handleResize = () => {
    setMediaType({
      isDesktopOrLaptop: window.matchMedia("(min-width: 1224px)").matches,
      isBigScreen: window.matchMedia("(min-width: 1824px)").matches,
      isTabletOrMobile: window.matchMedia("(max-width: 1224px)").matches,
      isPortrait: window.matchMedia("(orientation: portrait)").matches,
      isRetina: window.matchMedia("(min-resolution: 2dppx)").matches,
    });
  };

  useEffect(() => {
    handleResize(); // Initial check
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    updateMediatype(mediaType);
  }, [mediaType, updateMediatype]);
}

export default MediaType;
