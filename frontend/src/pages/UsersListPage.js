import PageLayout from "../components/PageLayout";
import profile from "../images/profile_icon.png";
import { FormattedMessage } from "react-intl";
import MessageUserButton from "../components/MessageUserButton";

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
    <PageLayout title={<FormattedMessage id="users.list.title" defaultMessage="Listagem de Utilizadores" />}>
      {/* Filtros */}
      <div className="flex gap-4 mb-4">
        <FormattedMessage id="users.filter.name" defaultMessage="Nome">
  {(msg) => (
    <input
      placeholder={msg}
      className="border px-2 py-1 rounded"
    />
  )}
</FormattedMessage>
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
        <FormattedMessage id="users.filter.manager" defaultMessage="Gestor">
  {(msg) => (
    <input
      placeholder={msg}
      className="border px-2 py-1 rounded"
    />
  )}
</FormattedMessage>
        <button className="bg-green-600 text-white px-3 rounded">
          <FormattedMessage id="users.button.excel" defaultMessage="Excel" />
        </button>
      </div>

      {/* Tabela */}
      <table className="w-full text-left border-collapse table-fixed">
        <thead>
          <tr className="bg-gray-200 text-sm">
            <th className="p-2 w-[180px]">
              <FormattedMessage id="users.table.name" defaultMessage="Nome" />
            </th>
            <th className="p-2 w-[140px]">
              <FormattedMessage id="users.table.office" defaultMessage="Escritório" />
            </th>
            <th className="p-2 w-[180px]">
              <FormattedMessage id="users.table.manager" defaultMessage="Gestor" />
            </th>
            <th className="p-2 w-[220px]">
              <FormattedMessage id="users.table.contact" defaultMessage="Contacto" />
            </th>
            <th className="p-2 w-[100px]"></th>
            <th className="p-2 w-[60px]"></th>
            <th className="p-2 w-[200px]">
              <FormattedMessage id="users.table.actions" defaultMessage="Ações" />
            </th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id} className="border-b hover:bg-gray-50">
              <td className="p-2">{user.name}</td>
              <td className="p-2">{user.office}</td>
              <td className="p-2">{user.manager}</td>
              <td className="p-2 truncate">{user.email}</td>
              <td className="p-2 pl-14">
  <img
    src={user.avatar}
    alt={user.name}
    className="w-8 h-8 rounded-full"
  />
</td>
<td className="p-2 text-center pl-14">
  <MessageUserButton userId={user.id} />
</td>
<td className="p-2 text-center pl-16">
  <button className="bg-[#D41C1C] text-white px-3 py-1 rounded flex items-center gap-2 ml-6">
    <FormattedMessage id="users.button.view" defaultMessage="Ver" /> <span>&gt;</span>
  </button>
</td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Paginação fake */}
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

