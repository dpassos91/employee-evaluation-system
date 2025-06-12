export default function AppButton({
  children,
  className = "",
  variant = "primary",
  ...props
}) {
  const base =
    "flex items-center font-semibold rounded transition-all duration-200 focus:outline-none";
  const variants = {
    primary: "bg-red-600 text-white hover:bg-red-700 px-6 py-3",
    secondary: "bg-gray-200 text-gray-800 hover:bg-gray-300 px-6 py-3",
    sidebar: "w-full gap-3 px-4 py-2 text-sm hover:bg-white/10 mb-1",
  };
  return (
    <button className={`${base} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}