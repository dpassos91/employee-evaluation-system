import { render, screen } from "@testing-library/react";
import PageFadeIn from "./PageFadeIn";

describe("PageFadeIn", () => {
  test("renderiza os filhos corretamente", () => {
    render(
      <PageFadeIn>
        <p>Conteúdo de teste</p>
      </PageFadeIn>
    );

    expect(screen.getByText("Conteúdo de teste")).toBeInTheDocument();
  });
});
