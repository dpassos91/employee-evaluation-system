import React from "react";
import { render, screen } from "@testing-library/react";
import SubmitButton from "./SubmitButton";
import { IntlProvider } from "react-intl";

const messages = {
  "form.submit": "Submit",
};

function renderWithIntl(ui) {
  return render(
    <IntlProvider locale="en" messages={messages}>
      {ui}
    </IntlProvider>
  );
}

describe("SubmitButton", () => {
  test("renders button with default label", () => {
    renderWithIntl(<SubmitButton id="unknown.id" defaultLabel="Default Label" />);
    expect(screen.getByRole("button")).toHaveTextContent("Default Label");
  });

  test("renders button with translated message", () => {
    renderWithIntl(<SubmitButton id="form.submit" defaultLabel="Default Label" />);
    expect(screen.getByRole("button")).toHaveTextContent("Submit");
  });
});
