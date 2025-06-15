import Sidebar from "./Sidebar";

export default function PageLayout({ title, subtitle, children }) {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <main className="ml-64 flex-1 p-8 pl-[105px] pr-[105px] pt-24">
        {/* Header comum */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold">{title}</h2>
            {subtitle && <p className="text-gray-600">{subtitle}</p>}
          </div>
        </div>
        {children}
      </main>
    </div>
  );
}

