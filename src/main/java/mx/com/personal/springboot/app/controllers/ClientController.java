package mx.com.personal.springboot.app.controllers;

import java.util.Map;

import javax.validation.Valid;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.support.SessionStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mx.com.personal.springboot.app.util.paginator.PageRender;
import mx.com.personal.springboot.app.models.entity.Cliente;
import mx.com.personal.springboot.app.models.service.IClientService;
import mx.com.personal.springboot.app.models.service.IUploadFileService;

@Controller
@SessionAttributes("cliente")
public class ClientController {
	@Autowired
	private IUploadFileService uploadFile;
	
	private static final String LISTAR = "client/listar";// Template
	private static final String FORM = "client/form";// Template
	private static final String VER = "client/ver";// Template

	private static final String SUCCESS = "success";
	private static final String SUCCESS_CREATE = "Cliente creado con exito";
	private static final String SUCCESS_UPDATE = "Cliente actualizado con exito";
	private static final String SUCCESS_DELETE = "Cliente eliminado con exito";

	private static final String PAGE = "page";

	private static final String INFO = "info";

	private static final String ERROR = "error";
	private static final String ERROR_ID = "El id del cliente no puede ser 0";
	private static final String ERROR_ID_NO_EXISTE = "El id del cliente no existe";

	// Redirect al metodo de listar
	private static final String FORM_SAVED = "redirect:list";

	private static final String URL_LISTAR = "/client/list";// Metodo de listar
	private static final String URL_FORM = "/client/form";// Metodo de form
	private static final String URL_ELIMINAR = "/client/eliminar/{id}";// Metodo eliminar

	private static final String FORM_TITLE = "Formulario de Cliente";

	private static final String LABEL_TITLE = "titulo";

	@Autowired
	private IClientService clientService;

	@RequestMapping(value = URL_LISTAR, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		Pageable pageRequest = PageRequest.of(page, 5);
		Page<Cliente> clientes = clientService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>(URL_LISTAR, clientes);

		model.addAttribute(LABEL_TITLE, "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute(PAGE, pageRender);
		return LISTAR;
	}

	@RequestMapping(value = URL_FORM)
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put(LABEL_TITLE, FORM_TITLE);
		model.put("cliente", cliente);

		return FORM;
	}

	@RequestMapping(value = URL_FORM, method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute(LABEL_TITLE, FORM_TITLE);
			return FORM;
		}

		if (foto != null & !foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadFile.delete(cliente.getFoto());
			}
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFile.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}

			flash.addFlashAttribute(INFO, "Imagen subida correctamente el archivo " + uniqueFilename);
			cliente.setFoto(uniqueFilename);
		}

		String mensaje = cliente.getId() == null ? SUCCESS_CREATE : SUCCESS_UPDATE;

		clientService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute(SUCCESS, mensaje);
		return FORM_SAVED;
	}

	@RequestMapping(value = URL_FORM + "/{id}", method = RequestMethod.GET)
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clientService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute(ERROR, ERROR_ID_NO_EXISTE);
				return FORM_SAVED;
			}
		} else {
			flash.addFlashAttribute(ERROR, ERROR_ID);
			return FORM_SAVED;
		}

		model.addAttribute(LABEL_TITLE, FORM_TITLE);
		model.addAttribute("cliente", cliente);

		return FORM;
	}

	@RequestMapping(value = URL_ELIMINAR)
	public String eliminar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		if (id > 0) {
			Cliente cliente = clientService.findOne(id);
			clientService.delete(id);
			flash.addFlashAttribute(SUCCESS, SUCCESS_DELETE);

			if (uploadFile.delete(cliente.getFoto())) {
				flash.addFlashAttribute(INFO, "Foto " + cliente.getFoto() + " eliminada");
			}
		}

		return "redirect:../list";
	}

	@GetMapping(value = "/client/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clientService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute(ERROR, ERROR_ID_NO_EXISTE);
				return "redirect:../list";
			}
		} else {
			flash.addFlashAttribute(ERROR, ERROR_ID);
			return "redirect:../list";
		}

		model.addAttribute("cliente", cliente);
		model.addAttribute(LABEL_TITLE, "Detalle cliente " + cliente.getNombre());
		return VER;
	}

	@GetMapping(value = "/client/uploads/{filename.*}")
	public ResponseEntity<Resource> verFoto(@PathVariable("filename.*") String filename) {
		Resource recurso = null;
		try {
			recurso = uploadFile.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}
}
