package aor.projetofinal.util;

import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to export evaluation data into a printable PDF format.
 */
public class PdfExportUtil {

    /**
     * Builds a PDF file (as byte array) containing detailed evaluation information.
     *
     * @param evaluation The evaluation to export.
     * @return A byte[] containing the PDF file.
     */
    public static byte[] buildEvaluationPdf(EvaluationEntity evaluation) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Employee Evaluation Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Evaluated info
            UserEntity evaluated = evaluation.getEvaluated();
            String evaluatedName = evaluated.getProfile().getFirstName() + " " + evaluated.getProfile().getLastName();
            String evaluatedEmail = evaluated.getEmail();

            Paragraph userInfo = new Paragraph(
                    String.format("Name: %s\nEmail: %s\n", evaluatedName, evaluatedEmail),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)
            );
            userInfo.setSpacingAfter(10f);
            document.add(userInfo);

            // Cycle info
            Paragraph cycleInfo = new Paragraph(
                    String.format("Cycle Number: %d\nCycle End Date: %s\n",
                            evaluation.getCycle().getId(),
                            evaluation.getCycle().getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)
            );
            cycleInfo.setSpacingAfter(10f);
            document.add(cycleInfo);

            // Grade
            String gradeLabel = evaluation.getGrade().getDescription(); // e.g., "Contribuição conforme o esperado"
            Paragraph gradeInfo = new Paragraph("Grade: " + gradeLabel,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            gradeInfo.setSpacingAfter(10f);
            document.add(gradeInfo);

            // Evaluator
            String evaluatorName = evaluation.getEvaluator() != null && evaluation.getEvaluator().getProfile() != null
                    ? evaluation.getEvaluator().getProfile().getFirstName() + " " + evaluation.getEvaluator().getProfile().getLastName()
                    : "N/A";

            Paragraph evaluatorInfo = new Paragraph("Evaluator: " + evaluatorName,
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            evaluatorInfo.setSpacingAfter(10f);
            document.add(evaluatorInfo);

            // Feedback section
            String feedback = evaluation.getFeedback() != null ? evaluation.getFeedback() : "No feedback available.";
            Paragraph feedbackSection = new Paragraph("Feedback:\n" + feedback,
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            feedbackSection.setSpacingBefore(20f);
            document.add(feedbackSection);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace(); // ou logger.error(...)
            return new byte[0]; // retorno vazio em caso de erro
        }
    }
}
