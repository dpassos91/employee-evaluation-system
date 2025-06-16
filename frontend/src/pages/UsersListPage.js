import PageLayout from "../components/PageLayout";
import profile from "../images/profile_icon.png";


export default function UsersPage() {
  const users = [
    {
      id: 1,
      name: "José Simples",
      office: "Coimbra",
      manager: "Paula Tó",
      email: "jose.simples@skilap.h.com",
      avatar: profile,
    },
    {
      id: 2,
      name: "Hans Zimmer",
      office: "Munich",
      manager: "Greta T.",
      email: "hans.zimmer@skilap.h.com",
      avatar: profile,
    },
    {
      id: 3,
      name: "Rita Branco",
      office: "Lisboa",
      manager: "Alexandra Castro",
      email: "rita.branco@skilap.h.com",
      avatar: profile,
    },
    {
      id: 4,
      name: "Nick Smith",
      office: "Boston",
      manager: "Javier Delgado",
      email: "nick.smith@skilap.h.com",
      avatar: profile,
    },
  ];

  return (
    <PageLayout title="Listagem de Utilizadores">
      {/* Filtros */}
      <div className="flex gap-4 mb-4">
        <input placeholder="Nome" className="border px-2 py-1 rounded" />
        <select className="border px-2 py-1 rounded">
                    <option value=""> </option>
                  <option value="Boston">Boston</option>
                  <option value="Coimbra">Coimbra</option>
                  <option value="Lisboa">Lisboa</option>
                  <option value="Munich">Munich</option>
                  <option value="Porto">Porto</option>
                  <option value="Southampton">Southampton</option>
                  <option value="Viseu">Viseu</option>
        </select>
        <input placeholder="Gestor" className="border px-2 py-1 rounded" />
        <button className="bg-green-600 text-white px-3 rounded">Excel</button>
      </div>

      {/* Tabela */}
<table className="w-full text-left border-collapse table-fixed">
  <thead>
    <tr className="bg-gray-200 text-sm">
      <th className="p-2 w-[180px]">Nome</th>
      <th className="p-2 w-[140px]">Escritório</th>
      <th className="p-2 w-[180px]">Gestor</th>
      <th className="p-2 w-[220px]">Contacto</th> {/* Email */}
      <th className="p-2 w-[60px]"></th>           {/* Ícone */}
      <th className="p-2 w-[100px]"></th>          {/* Botão */}
    </tr>
  </thead>
  <tbody>
    {users.map((user) => (
      <tr key={user.id} className="border-b hover:bg-gray-50">
        <td className="p-2">{user.name}</td>
        <td className="p-2">{user.office}</td>
        <td className="p-2">{user.manager}</td>

        {/* Email */}
        <td className="p-2 truncate">{user.email}</td>

        {/* Ícone */}
        <td className="p-2 text-center">
          <img
            src={user.avatar}
            alt={user.name}
            className="w-6 h-6 rounded-full mx-auto"
          />
        </td>

        {/* Botão Ver */}
        <td className="p-2">
          <button className="bg-[#D41C1C] text-white px-3 py-1 rounded flex items-center gap-2 ml-12">
            Ver <span>&gt;</span>
          </button>
        </td>
      </tr>
    ))}
  </tbody>
</table>


      {/* Paginação fake */} {/*BACKEND BACKEND BACKEND BACKEND BACKEND BACKEND BACKEND BACKEND BACKEND*/}
      <div className="mt-4 flex justify-end gap-2 text-blue-700 text-sm">
        <button className="hover:underline">1</button>
        <button className="hover:underline">2</button>
        <span>...</span>
        <button className="hover:underline">10</button>
        <button className="hover:underline">{">"}</button>
      </div>
    </PageLayout>
  );
}
