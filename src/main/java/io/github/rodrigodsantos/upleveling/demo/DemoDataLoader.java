package io.github.rodrigodsantos.upleveling.demo;

import io.github.rodrigodsantos.upleveling.gamification.CheckIn;
import io.github.rodrigodsantos.upleveling.gamification.CheckInRepository;
import io.github.rodrigodsantos.upleveling.gamification.StreakCalculator;
import io.github.rodrigodsantos.upleveling.habit.Habit;
import io.github.rodrigodsantos.upleveling.habit.HabitRepository;
import io.github.rodrigodsantos.upleveling.shared.DemoAccount;
import io.github.rodrigodsantos.upleveling.user.User;
import io.github.rodrigodsantos.upleveling.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

/**
 * Recria a conta demo toda vez que a API sobe com o perfil "demo" (só no deploy).
 * <p>
 * Os dados dependem da data (um streak gravado hoje vira 0 em uma semana), então os check-ins dos últimos
 * 30 dias são gerados de novo, relativos a "hoje". O Render gratuito dorme e acorda com frequência,
 * e a cada vez a conta demo volta atualizada.
 */
@Component
@Profile("demo")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private static final int HISTORY_DAYS = 30;
    private static final int BONUS_EVERY_DAYS = 7;
    private static final Set<DayOfWeek> EVERY_DAY = Set.of();

    /**
     * Um hábito da demo e o "comportamento" do usuário fictício com ele.
     *
     * @param chance       probabilidade de cumprir a meta num dia comum
     * @param recentStreak últimos N dias (antes de hoje) sempre cumpridos, para ter streak para mostrar
     * @param todayCount   check-ins já feitos hoje (o resumo do dia aparece "em andamento")
     */
    private record DemoHabit(String name, int xpReward, int dailyTarget, Set<DayOfWeek> days, double chance,
                             int recentStreak, int todayCount) {
    }

    private static final List<DemoHabit> HABITS = List.of(
            new DemoHabit("Ler 20 páginas", 30, 1, EVERY_DAY, 0.8, 16, 1),
            new DemoHabit("Beber água", 5, 8, EVERY_DAY, 0.7, 4, 3),
            new DemoHabit("Academia", 50, 1, Set.of(MONDAY, WEDNESDAY, FRIDAY), 0.85, 6, 0),
            new DemoHabit("Meditar 10 minutos", 20, 1, EVERY_DAY, 0.6, 2, 0),
            new DemoHabit("Estudar Java", 40, 1, Set.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), 0.8, 5, 0)
    );

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final CheckInRepository checkInRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DemoDataLoader(UserRepository userRepository, HabitRepository habitRepository,
                          CheckInRepository checkInRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
        this.checkInRepository = checkInRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        reset();
    }

    /** Apaga os hábitos e check-ins da conta demo e gera tudo de novo. Sempre o mesmo resultado para a mesma data. */
    @Transactional
    public void reset() {
        LocalDate today = LocalDate.now(clock);
        User user = demoUser();

        checkInRepository.deleteAllOfUser(user.getId());
        habitRepository.deleteAll(habitRepository.findByUserId(user.getId()));
        habitRepository.flush();

        // Semente fixa: os "sorteios" saem iguais a cada execução (a demo não muda a cada reinício)
        Random random = new Random(42);
        List<CheckIn> checkIns = new ArrayList<>();
        for (DemoHabit demo : HABITS) {
            Habit habit = habitRepository.save(
                    new Habit(user.getId(), demo.name(), demo.xpReward(), demo.dailyTarget(), demo.days()));
            checkIns.addAll(history(user, habit, demo, today, random));
        }
        checkInRepository.saveAll(checkIns);

        log.info("Conta demo ({}) recriada: {} hábitos e {} check-ins", DemoAccount.EMAIL, HABITS.size(), checkIns.size());
    }

    private User demoUser() {
        User user = userRepository.findByEmail(DemoAccount.EMAIL)
                .orElseGet(() -> userRepository.save(new User("Rodrigo", DemoAccount.EMAIL, "")));
        // Garante a senha conhecida (a API já impede que um visitante a troque, mas a demo se protege sozinha)
        user.changePassword(passwordEncoder.encode(DemoAccount.PASSWORD));
        return user;
    }

    /** Gera os check-ins do mais antigo para hoje, calculando o bônus de streak como o CheckInService faz. */
    private List<CheckIn> history(User user, Habit habit, DemoHabit demo, LocalDate today, Random random) {
        List<CheckIn> checkIns = new ArrayList<>();
        Set<LocalDate> completed = new HashSet<>();

        for (int daysAgo = HISTORY_DAYS; daysAgo >= 0; daysAgo--) {
            LocalDate day = today.minusDays(daysAgo);
            if (!habit.isScheduledOn(day.getDayOfWeek())) {
                continue;
            }
            int count = checkInsOn(demo, daysAgo, random);
            for (int i = 1; i <= count; i++) {
                int bonus = 0;
                if (i == demo.dailyTarget()) {
                    completed.add(day);
                    int streak = StreakCalculator.currentStreak(completed, demo.days(), day);
                    if (streak % BONUS_EVERY_DAYS == 0) {
                        bonus = demo.xpReward() * demo.dailyTarget() / 2;
                    }
                }
                checkIns.add(new CheckIn(habit.getId(), user.getId(), day, demo.xpReward(), bonus));
            }
        }
        return checkIns;
    }

    private int checkInsOn(DemoHabit demo, int daysAgo, Random random) {
        if (daysAgo == 0) {
            return demo.todayCount();
        }
        if (daysAgo <= demo.recentStreak() || random.nextDouble() < demo.chance()) {
            return demo.dailyTarget();
        }
        // Dia "fraco": em hábitos com meta maior que 1, fez só uma parte
        return demo.dailyTarget() > 1 ? random.nextInt(demo.dailyTarget()) : 0;
    }
}
