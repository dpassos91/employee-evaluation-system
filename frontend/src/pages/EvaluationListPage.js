import { useState, useMemo } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import MessageUserButton from "../components/MessageUserButton";


import { apiConfig } from "../api/apiConfig";
import { useNavigate } from "react-router-dom";




export default function EvaluationListPage() {
// Filtros e página
  const [name, setName] = useState("");
  const [evaluationState, setEvaluationState] = useState("");
  const [grade, setGrade] = useState("");
  const [page, setPage] = useState(1);
  const navigate = useNavigate(); 

    const filters = useMemo(
    () => ({ name, evaluationState, grade, page }),
    [name, evaluationState, grade, page]
  );








  return (
    <PageLayout
       title={<FormattedMessage id="users.list.title" defaultMessage="Listagem de Avaliações" />}
    >




          </PageLayout>
          );
        }

