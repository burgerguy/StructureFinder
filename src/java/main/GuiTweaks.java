package main;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class GuiTweaks {
	
	static class LimitDocumentFilter extends DocumentFilter {
		
		private final int limit;
		
		public LimitDocumentFilter(int limit) {
			if (limit <= 0) {
				throw new IllegalArgumentException("Limit can not be <= 0");
			}
			this.limit = limit;
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			int currentLength = fb.getDocument().getLength();
			int overLimit = currentLength + text.length() - limit - length;
			if (overLimit > 0) {
				text = text.substring(0, text.length() - overLimit);
			}
			if (text.length() > 0) {
				super.replace(fb, offset, length, text, attrs);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static class ConditionalComboBoxRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = -1384299159610756056L;
		private final ListCellRenderer defaultComboBoxRenderer = new JComboBox<>().getRenderer();
		
		@Override
		@SuppressWarnings("unchecked")
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			
			boolean itemEnabled = true;
			
			try {
				itemEnabled = ((Conditionable) value).isEnabled();
			} catch (NullPointerException e) {
				itemEnabled = false;
			}
			
			Component c = defaultComboBoxRenderer.getListCellRendererComponent(list, value, index,
					isSelected && itemEnabled, cellHasFocus);
			
			if (!itemEnabled) {
				c.setBackground(list.getBackground());
				c.setForeground(UIManager.getColor("Label.disabledForeground"));
			}
			
			return c;
		}
	}
	
	static class ConditionalComboBoxListener implements ActionListener {
		JComboBox<ConditionalString> combobox;
		Object oldItem;
		
		ConditionalComboBoxListener(JComboBox<ConditionalString> combobox) {
			this.combobox = combobox;
			combobox.setSelectedIndex(0);
			oldItem = combobox.getSelectedItem();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object selectedItem = combobox.getSelectedItem();
			if (!((Conditionable) selectedItem).isEnabled()) {
				combobox.setSelectedItem(oldItem);
			} else {
				oldItem = selectedItem;
			}
		}
	}
	
	static class ConditionalString implements Conditionable {
		String string;
		boolean isEnabled;
		
		ConditionalString(String string, boolean isEnabled) {
			this.string = string;
			this.isEnabled = isEnabled;
		}
		
		ConditionalString(String string) {
			this(string, true);
		}
		
		@Override
		public boolean isEnabled() {
			return isEnabled;
		}
		
		@Override
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	interface Conditionable {
		public boolean isEnabled();
		
		public void setEnabled(boolean enabled);
		
		@Override
		public String toString();
	}
}