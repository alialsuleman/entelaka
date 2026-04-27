package com.ali.antelaka.library;

import com.ali.antelaka.library.CodeExample;
import com.ali.antelaka.library.CodeExampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final CodeExampleRepository repository;

    // تحويل Entity إلى DTO
    private CodeExampleDTO convertToDTO(CodeExample entity) {
        return new CodeExampleDTO(
                entity.getId(),
                entity.getLanguage(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCode()
        );
    }

    // تحويل DTO إلى Entity
    private CodeExample convertToEntity(CodeExampleDTO dto) {
        CodeExample entity = new CodeExample();
        entity.setId(dto.getId());
        entity.setLanguage(dto.getLanguage());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCode(dto.getCode());
        return entity;
    }

    // CREATE - إضافة مثال جديد
    @Transactional
    public CodeExampleDTO createCodeExample(CodeExampleDTO dto) {
        CodeExample entity = convertToEntity(dto);
        CodeExample saved = repository.save(entity);
        return convertToDTO(saved);
    }

    // READ - جلب كل الأمثلة
    public List<CodeExampleDTO> getAllCodeExamples() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // READ - جلب مثال بواسطة ID
    public CodeExampleDTO getCodeExampleById(Long id) {
        CodeExample entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code example not found with id: " + id));
        return convertToDTO(entity);
    }

    // READ - جلب أمثلة حسب اللغة
    public List<CodeExampleDTO> getCodeExamples(String language) {
        List<CodeExample> examples = repository.findByLanguageIgnoreCase(language);

        return examples.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE - تحديث مثال كامل
    @Transactional
    public CodeExampleDTO updateCodeExample(Long id, CodeExampleDTO dto) {
        CodeExample existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code example not found with id: " + id));

        existing.setLanguage(dto.getLanguage());
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setCode(dto.getCode());

        CodeExample updated = repository.save(existing);
        return convertToDTO(updated);
    }

    // UPDATE - تحديث جزئي
    @Transactional
    public CodeExampleDTO patchCodeExample(Long id, CodeExampleDTO dto) {
        CodeExample existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code example not found with id: " + id));

        if (dto.getLanguage() != null) existing.setLanguage(dto.getLanguage());
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getCode() != null) existing.setCode(dto.getCode());

        CodeExample updated = repository.save(existing);
        return convertToDTO(updated);
    }

    // DELETE - حذف مثال حسب ID
    @Transactional
    public void deleteCodeExample(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Code example not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // DELETE - حذف كل الأمثلة للغة معينة
    @Transactional
    public void deleteByLanguage(String language) {
        repository.deleteByLanguageIgnoreCase(language);
    }

    // البحث
    public List<CodeExampleDTO> searchExamples(String language, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getCodeExamples(language);
        }
        return repository.searchByLanguageAndTitle(language, searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // إحصائيات
    public long getCountByLanguage(String language) {
        return repository.countByLanguageIgnoreCase(language);
    }

    // أمثلة افتراضية للغة محددة (إذا كانت قاعدة البيانات فارغة)



}