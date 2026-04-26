package Parsers.ParserTopDown.utils;

import java.util.List;

public final class AstVisualizer {
	private AstVisualizer() {
	}

	public static String render(Program program) {
		StringBuilder sb = new StringBuilder();
		if (program == null) {
			return "<null Program>";
		}

		sb.append("Program").append(System.lineSeparator());
		List<Node> forms = program.forms();
		if (forms == null || forms.isEmpty()) {
			sb.append("(empty)");
			return sb.toString();
		}

		for (int i = 0; i < forms.size(); i++) {
			Node form = forms.get(i);
			boolean last = (i == forms.size() - 1);
			renderNode(form, sb, "", last, "Form[" + i + "]");
		}

		return sb.toString();
	}

	private static void renderNode(Node node, StringBuilder sb, String prefix, boolean isLast, String edgeLabel) {
		String branch = isLast ? "`-- " : "|-- ";
		sb.append(prefix).append(branch);
		if (edgeLabel != null && !edgeLabel.isBlank()) {
			sb.append(edgeLabel).append(": ");
		}
		sb.append(summary(node)).append(System.lineSeparator());

		String childPrefix = prefix + (isLast ? "    " : "|   ");
		renderChildren(node, sb, childPrefix);
	}

	private static void renderChildren(Node node, StringBuilder sb, String prefix) {
		if (node == null) return;

		if (node instanceof Program p) {
			List<Node> forms = p.forms();
			if (forms == null) return;
			for (int i = 0; i < forms.size(); i++) {
				boolean last = (i == forms.size() - 1);
				renderNode(forms.get(i), sb, prefix, last, "Form[" + i + "]");
			}
			return;
		}

		if (node instanceof QuoteExpr q) {
			renderNode(q.datum(), sb, prefix, true, "datum");
			return;
		}

		if (node instanceof VectorExpr v) {
			renderNodeList(v.elements(), sb, prefix, "elem");
			return;
		}

		if (node instanceof ListExpr l) {
			renderNodeList(l.elements(), sb, prefix, "elem");
			return;
		}

		if (node instanceof DottedListExpr d) {
			List<Node> prefixNodes = d.prefix();
			if (prefixNodes != null) {
				for (int i = 0; i < prefixNodes.size(); i++) {
					renderNode(prefixNodes.get(i), sb, prefix, false, "prefix[" + i + "]");
				}
			}
			renderNode(d.tail(), sb, prefix, true, "tail");
			return;
		}
	}

	private static void renderNodeList(List<Node> nodes, StringBuilder sb, String prefix, String labelBase) {
		if (nodes == null || nodes.isEmpty()) {
			sb.append(prefix).append("`-- ").append("(empty)").append(System.lineSeparator());
			return;
		}

		for (int i = 0; i < nodes.size(); i++) {
			boolean last = (i == nodes.size() - 1);
			renderNode(nodes.get(i), sb, prefix, last, labelBase + "[" + i + "]");
		}
	}

	private static String summary(Node node) {
		if (node == null) return "<null>";

		if (node instanceof LangDirective ld) {
			return "LangDirective language='" + safe(ld.languageLexeme()) + "'";
		}
		if (node instanceof Atom a) {
			return "Atom kind=" + a.kind() + " token=" + a.tokenType() + " lexeme='" + safe(a.lexeme()) + "'";
		}
		if (node instanceof QuoteExpr q) {
			return "Quote kind=" + q.kind();
		}
		if (node instanceof VectorExpr v) {
			int n = v.elements() == null ? 0 : v.elements().size();
			return "Vector size=" + n;
		}
		if (node instanceof ListExpr l) {
			int n = l.elements() == null ? 0 : l.elements().size();
			return "List delim=" + l.delim() + " size=" + n;
		}
		if (node instanceof DottedListExpr d) {
			int n = d.prefix() == null ? 0 : d.prefix().size();
			return "DottedList delim=" + d.delim() + " prefixSize=" + n;
		}
		if (node instanceof Program p) {
			int n = p.forms() == null ? 0 : p.forms().size();
			return "Program forms=" + n;
		}

		return node.getClass().getSimpleName();
	}

	private static String safe(String s) {
		if (s == null) return "";
		return s
				.replace("\\", "\\\\")
				.replace("\r", "\\r")
				.replace("\n", "\\n")
				.replace("\t", "\\t")
				.replace("'", "\\'");
	}
}
