/**
 * 
 */
package de.danielsenff.radds.view.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.TextureImage;
import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import ddsutil.BIUtil;
import ddsutil.ImageOperations;
import de.danielsenff.radds.Radds;
import de.danielsenff.radds.RaddsView;
import de.danielsenff.radds.models.ColorChannel;
import de.danielsenff.radds.tasks.LoadImageTask;
import de.danielsenff.radds.util.FileDrop;

/**
 * Control-Panel for the {@link BICanvas}.
 * This contains controls and components to manipulate the canvas. 
 * @author Daniel Senff
 *
 */
public class CanvasControlsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3204480115770320549L;
	private BICanvas canvas;
	private JComboBox<Integer> zoomCombo;
	private JSlider zoomSlider;
	private RaddsView view;
	private JComboBox<Integer> mipMapCombo;
	private JScrollPane scrollViewPane;
	private boolean fitSize;

	/**
	 * @param view
	 */
	public CanvasControlsPanel(final RaddsView view) {
		this.view = view;

		setLayout(new BorderLayout());
		scrollViewPane = initScrollCanvas();
		final JPanel navigateCanvas = initNavigationPanel();

		getCanvas().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pChangeEvent) {
				if(pChangeEvent.getPropertyName().equals("zoomFactor")){
					float newValue = (Float) pChangeEvent.getNewValue();
					Integer percentage = Integer.valueOf((int) (newValue * 100));
					zoomSlider.setValue(percentage);
					zoomCombo.setSelectedItem("" +percentage);
				}
			}
		});

		new FileDrop( scrollViewPane, new FileDrop.Listener(){ 
			@Override
			public void filesDropped( java.io.File[] files ) {   
				// handle first file
				openImage(files[0]);
			}   // end filesDropped
		}); // end FileDrop.Listener

		this.add(scrollViewPane, BorderLayout.CENTER);
		this.add(navigateCanvas, BorderLayout.SOUTH);
	}

	private void openImage(File file) {
		LoadImageTask convertTask = new LoadImageTask(file);
		TaskService ts = Radds.getApplication().getContext().getTaskService();
		ts.execute(convertTask);
	}

	/**
	 * Initialize components in the navigation panel for this BICanvas. 
	 * @return
	 */
	private JPanel initNavigationPanel() {
		
		// Action buttons
		
		MigLayout miglayout = new MigLayout("nogrid, fillx, wrap, gap 0, ins 0");
		final JPanel panel = new JPanel();
		panel.setLayout(miglayout);
		final JButton copyButton = new JButton(view.getAction("copy"));
		panel.add(copyButton, "sg");

		final JButton bgColorButton = initTransparencyColorButton();
		panel.add(bgColorButton, "sg");

		// Color Channel
		
		final JLabel lblChannelCombo = new JLabel(getResourceMap().getString("Channels")+":");
		panel.add(lblChannelCombo, "gap push");
		final JComboBox<ColorChannel> channelCombo = initColorChannelCombo();
		panel.add(channelCombo, "sg");

		// MipMaps
		
		JLabel lblMipMap = new JLabel(getResourceMap().getString("MipMap")+":");
		panel.add(lblMipMap, "gap push");

		initMipmapCombo();
		panel.add(mipMapCombo, "sg, wrap");

		// ZO_om
		
		JLabel lblZoom = new JLabel(getResourceMap().getString("Zoom")+":"); 
		panel.add(lblZoom, "");

		initZoomCombo();
		panel.add(zoomCombo, "");

		initZoomSlider();
		panel.add(zoomSlider, "");
		
		JCheckBox chkZoomFitSize = initFitZoomCheckbox();
		panel.add(chkZoomFitSize, "");

		return panel;
	}

	private JCheckBox initFitZoomCheckbox() {
		JCheckBox chkZoomFitSize = new JCheckBox(getResourceString("Zoom_to_fit"));
		chkZoomFitSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				fitSize = ((JCheckBox)e.getSource()).isSelected();
				resizeCanvasToFit(fitSize);
			}

		});
		return chkZoomFitSize;
	}

	private JButton initTransparencyColorButton() {
		final JButton bgColorButton = new JButton(getResourceString("Background"));
		bgColorButton.setToolTipText(getResourceString("set_background_color"));
		bgColorButton.setIcon(initColorImageIcon(canvas.getTransparencyColor()));
		bgColorButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BICanvas canvas = getCanvas();
				Color returnValue = null;
				try {
					returnValue = JColorChooser.showDialog(canvas, 
							getResourceString("set_background_color"), 
							canvas.getTransparencyColor());
					if(returnValue != null) {
						canvas.setTransparencyColor(returnValue);
						canvas.repaint();
						bgColorButton.setIcon(initColorImageIcon(returnValue));
						bgColorButton.repaint();
					} 
				} catch (Exception ex) {

				}
			}
		});
		return bgColorButton;
	}

	private JComboBox<ColorChannel> initColorChannelCombo() {
		final JComboBox<ColorChannel> channelCombo = new JComboBox<ColorChannel>(composeColorChannelModel());
		channelCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				final JComboBox<ColorChannel> channelCombo = (JComboBox<ColorChannel>) event.getSource();
				final ColorChannel channel = (ColorChannel) channelCombo.getSelectedItem();
				canvas.setChannelMode(channel.getChannel());
			}

		});
		return channelCombo;
	}


	private ImageIcon initColorImageIcon(Color color) {
		BufferedImage colorImage = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		for (int y = 0; y < colorImage.getHeight(); y++) {
			for (int x = 0; x < colorImage.getWidth(); x++) {
				colorImage.setRGB(x, y, color.getRGB());
			}
		}

		return new ImageIcon(colorImage);
	}

	private void initZoomSlider() {
		zoomSlider = new JSlider(0, 500, 100);
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		labels.put(10, new JLabel("0.1x"));
		labels.put(100, new JLabel("1x"));
		labels.put(250, new JLabel("2.5x"));
		labels.put(500, new JLabel("5x"));
		zoomSlider.setLabelTable(labels);
		zoomSlider.setMajorTickSpacing(100);
		zoomSlider.setMinorTickSpacing(25);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setPreferredSize(new Dimension(250, 14));
		zoomSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final int zoomValue = ((JSlider)e.getSource()).getValue();
				canvas.setZoomFactor( (Float.valueOf(zoomValue)) * 0.01f);
				canvas.revalidate();
			}

		});
	}

	private void initMipmapCombo() {
		mipMapCombo = new JComboBox<Integer>();
		updateNumMipMaps();
		mipMapCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(mipMapCombo.getItemCount() > 0) {
					int index = (Integer) ((JComboBox<Integer>)e.getSource()).getSelectedItem();
					TextureImage textureImage = view.getTextureImage();
					if(index < textureImage.getNumMipMaps()) {
						BufferedImage bi = textureImage.getMipMap(index-1);
						canvas.setSourceBI(bi);
						canvas.revalidate();
					}
				}
			}
		});
	}

	private void initZoomCombo() {
		final String[] defaultZooms = { "25", "50", "100", "150", "200", "400"};
		zoomCombo = new JComboBox(defaultZooms);
		zoomCombo.setSelectedIndex(2);
		zoomCombo.setEditable(true);
		zoomCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				float zoomValue = (Float.valueOf((String) zoomCombo.getSelectedItem())) / 100;
				canvas.setZoomFactor( zoomValue);
				canvas.repaint();
			}
		});
		zoomCombo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					float zoomValue = (Float.valueOf((String) zoomCombo.getSelectedItem())) / 100;
					canvas.setZoomFactor( zoomValue);
					canvas.repaint();
				}
			}
		});
	}



	private void resizeCanvasToFit(boolean fitSize) {
		float zoomValue;
		if (fitSize) {
			zoomValue= calculateZoomfactorFitWidth();
		} else {
			zoomValue = (Float.valueOf((String) zoomCombo.getSelectedItem())) * 0.01f;
		}
		canvas.setZoomFactor( zoomValue);
		canvas.repaint();
	}

	public float calculateZoomfactorFitWidth() {
		Insets insets = scrollViewPane.getBorder().getBorderInsets(canvas);
		float factorWidth = (float) ( (scrollViewPane.getSize().getWidth()-insets.left-insets.right) / canvas.getSource().getWidth());
		float factorHeight = (float) ( (scrollViewPane.getSize().getHeight()-insets.bottom-insets.top) / canvas.getSource().getHeight());
		
		return (factorWidth < factorHeight) ? factorWidth : factorHeight; 
	}

	private String getResourceString(final String text) {
		return getResourceMap().getString(text);
	}

	/**
	 * Number of MipMaps of the currently displayed texture.
	 */
	public void updateNumMipMaps() {
		TextureImage textureImage = view.getTextureImage();
		if(textureImage != null) {
			mipMapCombo.removeAllItems();
			for (int i = 0; i < textureImage.getNumMipMaps(); i++) {
				mipMapCombo.addItem(i+1);
			}
		}
	}


	/**
	 * Init ComboBox for selecting different Color Channels of an Image.
	 * @return
	 */
	private DefaultComboBoxModel<ColorChannel> composeColorChannelModel() {
		final DefaultComboBoxModel<ColorChannel> combo = new DefaultComboBoxModel<ColorChannel>();
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.RGB, getResourceMap().getString("rgb_channels")));
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.RED, getResourceMap().getString("r_channel")));
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.GREEN, getResourceMap().getString("g_channel")));
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.BLUE, getResourceMap().getString("b_channel")));
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.ALPHA, getResourceMap().getString("a_channel")));
		combo.addElement(new ColorChannel(ImageOperations.ChannelMode.RGBA, getResourceMap().getString("rgba_transparent")));
		return combo;
	}

	private ResourceMap getResourceMap() {
		final Radds instance = Radds.getInstance(Radds.class);
		final ApplicationContext context = instance.getContext();
		final org.jdesktop.application.ResourceMap resourceMap = context.getResourceMap(RaddsView.class);
		return resourceMap;
	}

	private ImageIcon getResourceIcon(String file){
		return new ImageIcon(Class.class.getResource(file));
	}

	private JScrollPane initScrollCanvas() {
		final ImageIcon defaultImage = (ImageIcon)getResourceMap().getIcon("Canvas.default_image"); 

		canvas = new BICanvas(BIUtil.convertImageToBufferedImage(defaultImage.getImage(), 
				BufferedImage.TYPE_4BYTE_ABGR));
		
		// JScrollPane
		final JScrollPane scrollViewPane = new JScrollPane(canvas);
		scrollViewPane.setPreferredSize(new Dimension(700,300));
		scrollViewPane.addComponentListener(new ComponentAdapter() {
		
			@Override
			public void componentResized(ComponentEvent event) {
				resizeCanvasToFit(fitSize);
			}
		});
		
		final ScrollCanvasListener scrollCanvasListener = new ScrollCanvasListener(scrollViewPane);
		canvas.addMouseMotionListener(scrollCanvasListener);
		//		canvas.addMouseWheelListener(scrollCanvasListener);
		canvas.addKeyListener(scrollCanvasListener);
		canvas.setFocusable(true);
		canvas.requestFocus();
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				canvas.requestFocus();
			}
		});
		canvas.addRenderedChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				resizeCanvasToFit(fitSize);
			}
		});
		

		canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		canvas.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(final AncestorEvent arg0) {}
			@Override
			public void ancestorMoved(final AncestorEvent arg0) {
				scrollViewPane.repaint();
			}
			@Override
			public void ancestorRemoved(final AncestorEvent arg0) {	}
		});

		

		
		return scrollViewPane;
	}

	/**
	 * Returns the BufferedImage drawing canvas
	 * @return
	 */
	public BICanvas getCanvas() {
		return this.canvas;
	}

}
