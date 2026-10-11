package com.acadexia.security;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Factory for attaching robust Swing DocumentFilters to text components
 * to enforce validation and prevent injection attacks.
 */
public final class InputFilterFactory {

    private InputFilterFactory() {}

    /**
     * Attaches visual feedback to flash a subtle red warning border on a component when an invalid input is blocked.
     */
    public static Consumer<String> createVisualFeedback(JComponent component, String fieldName) {
        Border defaultBorder = component.getBorder();
        return rejected -> {
            component.setBorder(new LineBorder(new Color(239, 68, 68), 2, true));
            component.setToolTipText("Disallowed character or potential exploit blocked: " + rejected);
            Timer timer = new Timer(1500, e -> {
                component.setBorder(defaultBorder);
                component.setToolTipText(null);
            });
            timer.setRepeats(false);
            timer.start();
        };
    }

    /**
     * Applies SQL Sanitization to any JTextField with length limit and optional visual feedback.
     */
    public static void applySqlSanitizer(JTextField field, int maxLength, boolean allowSpaces) {
        Consumer<String> feedback = createVisualFeedback(field, "Input");
        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new SqlSanitizerFilter(maxLength, allowSpaces, feedback));
        }
    }

    /**
     * Specialized filter for Register Numbers, Faculty IDs, and Usernames.
     * Permitted: A-Z, a-z, 0-9, dots (.), dashes (-), and underscores (_).
     */
    public static void applyIdentifierFilter(JTextField field, int maxLength) {
        Consumer<String> feedback = createVisualFeedback(field, "Identifier");
        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string != null && isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), string, offset, 0)) {
                        super.insertString(fb, offset, string, attr);
                    } else if (string != null) {
                        feedback.accept(string);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, null, attrs);
                        return;
                    }
                    if (isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), text, offset, length)) {
                        super.replace(fb, offset, length, text, attrs);
                    } else {
                        feedback.accept(text);
                    }
                }

                private boolean isValid(String current, String incoming, int offset, int len) {
                    if (current.length() - len + incoming.length() > maxLength) {
                        return false;
                    }
                    for (char c : incoming.toCharArray()) {
                        if (!Character.isLetterOrDigit(c) && c != '-' && c != '_' && c != '.') {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Specialized filter for Portal Login Identifier field (Username, Email, Phone, Register Number, or Faculty ID).
     * Permitted: A-Z, a-z, 0-9, dots, dashes, underscores, @, +, and spaces.
     * Blocks quotes, semicolons, backticks, null bytes, comments, and SQL injection strings.
     */
    public static void applyLoginIdentifierFilter(JTextField field, int maxLength) {
        Consumer<String> feedback = createVisualFeedback(field, "Login Identifier");
        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string != null && isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), string, offset, 0)) {
                        super.insertString(fb, offset, string, attr);
                    } else if (string != null) {
                        feedback.accept(string);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, null, attrs);
                        return;
                    }
                    if (isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), text, offset, length)) {
                        super.replace(fb, offset, length, text, attrs);
                    } else {
                        feedback.accept(text);
                    }
                }

                private boolean isValid(String current, String incoming, int offset, int len) {
                    if (current.length() - len + incoming.length() > maxLength) {
                        return false;
                    }
                    for (char c : incoming.toCharArray()) {
                        if (c == '\'' || c == '\"' || c == ';' || c == '`' || c == '\0' || c == '\\') {
                            return false;
                        }
                        if (!Character.isLetterOrDigit(c) && c != '-' && c != '_' && c != '.' && c != '@' && c != '+' && !Character.isWhitespace(c)) {
                            return false;
                        }
                    }
                    StringBuilder prospective = new StringBuilder(current);
                    prospective.replace(offset, offset + len, incoming);
                    String full = prospective.toString();
                    if (full.contains("--") || full.contains("/*") || full.contains("*/")) {
                        return false;
                    }
                    return !SecurityUtils.containsSqlInjection(full);
                }
            });
        }
    }

    /**
     * Strict Numeric filter for marks, credits, semester, phone.
     */
    public static void applyNumericFilter(JTextField field, int maxLength, boolean allowDecimal) {
        Consumer<String> feedback = createVisualFeedback(field, "Number");
        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string != null && isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), string, offset, 0)) {
                        super.insertString(fb, offset, string, attr);
                    } else if (string != null) {
                        feedback.accept(string);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, null, attrs);
                        return;
                    }
                    if (isValid(fb.getDocument().getText(0, fb.getDocument().getLength()), text, offset, length)) {
                        super.replace(fb, offset, length, text, attrs);
                    } else {
                        feedback.accept(text);
                    }
                }

                private boolean isValid(String current, String incoming, int offset, int len) {
                    if (current.length() - len + incoming.length() > maxLength) {
                        return false;
                    }
                    StringBuilder prospective = new StringBuilder(current);
                    prospective.replace(offset, offset + len, incoming);
                    String prospectiveStr = prospective.toString();
                    if (prospectiveStr.isEmpty()) return true;

                    if (allowDecimal) {
                        return prospectiveStr.matches("^\\d*(\\.\\d{0,2})?$");
                    } else {
                        return prospectiveStr.matches("^\\d+$");
                    }
                }
            });
        }
    }

    /**
     * Password filter: enforces max length and blocks null bytes and dangerous control chars.
     */
    public static void applyPasswordFilter(JPasswordField field, int maxLength) {
        Consumer<String> feedback = createVisualFeedback(field, "Password");
        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string != null && isValid(fb.getDocument().getLength() + string.length(), string)) {
                        super.insertString(fb, offset, string, attr);
                    } else if (string != null) {
                        feedback.accept(string);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, null, attrs);
                        return;
                    }
                    if (isValid(fb.getDocument().getLength() - length + text.length(), text)) {
                        super.replace(fb, offset, length, text, attrs);
                    } else {
                        feedback.accept(text);
                    }
                }

                private boolean isValid(int totalLen, String text) {
                    if (totalLen > maxLength) return false;
                    for (char c : text.toCharArray()) {
                        if (c == '\0' || c == '\'') return false;
                    }
                    return true;
                }
            });
        }
    }
}
